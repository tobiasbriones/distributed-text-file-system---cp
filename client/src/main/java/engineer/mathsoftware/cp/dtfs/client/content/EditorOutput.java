// Copyright (c) 2021 Tobias Briones. All rights reserved.
// SPDX-License-Identifier: BSD-3-Clause
// This file is part of https://github.com/tobiasbriones/cp-unah-mm545-distributed-text-file-system

package engineer.mathsoftware.cp.dtfs.client.content;

import engineer.mathsoftware.cp.dtfs.client.AppLocalFiles;
import engineer.mathsoftware.cp.dtfs.client.content.editor.Editor;
import engineer.mathsoftware.cp.dtfs.client.content.files.Files;
import engineer.mathsoftware.cp.dtfs.client.info.Info;
import engineer.mathsoftware.cp.dtfs.FileSystemService;
import engineer.mathsoftware.cp.dtfs.io.File;
import engineer.mathsoftware.cp.dtfs.io.file.Nothing;
import engineer.mathsoftware.cp.dtfs.io.file.Result;
import engineer.mathsoftware.cp.dtfs.io.file.text.TextFileContent;
import engineer.mathsoftware.cp.dtfs.io.file.text.TextFileRepository;
import javafx.application.Platform;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.function.Consumer;

/**
 * @author Tobias Briones
 */
final class EditorOutput implements Editor.Output {
    record DependencyConfig(
        TextFileRepository repository,
        Files.Input filesInput,
        Editor.Input editorInput,
        Info.Input infoInput
    ) {}

    private final TextFileRepository repository;
    private final Files.Input filesInput;
    private final Editor.Input editorInput;
    private final Info.Input infoInput;
    private FileSystemService service;

    EditorOutput(DependencyConfig config) {
        this.repository = config.repository();
        this.filesInput = config.filesInput();
        this.editorInput = config.editorInput();
        this.infoInput = config.infoInput();
        service = null;
    }

    @Override
    public void onFileAddedToChangelist(File file) {
        filesInput.update();
    }

    @Override
    public void onPush(File file) {
        if (service == null) {
            return;
        }
        if (file instanceof File.TextFile f) {
            pushFile(f);
        }
    }

    @Override
    public void onPull(File file) {
        if (service == null) {
            return;
        }
        if (file instanceof File.TextFile f) {
            pullFile(f);
        }
    }

    void setService(FileSystemService value) {
        service = value;
    }

    // ---------- PUSH
    private void pushFile(File.TextFile f) {
        final Result<TextFileContent> result = repository.get(f);

        if (result instanceof Result.Success<TextFileContent> s) {
            pushFileContentAsync(s.value());
        }
        else if (result instanceof Result.Failure<TextFileContent> fail) {
            fail.ifPresent(throwable -> infoInput.setError(throwable.getMessage()));
        }
    }

    private void pushFileContentAsync(TextFileContent content) {
        final Consumer<Result<Nothing>> resultConsumer = result -> {
            if (result instanceof Result.Success<Nothing>) {
                Platform.runLater(() -> onFilePushed(content.file()));
            }
            else {
                Platform.runLater(() -> onFilePushFailed(content.file()));
            }
        };
        final Runnable runnable = () -> {
            try {
                final Result<Nothing> result = service.writeTextFile(content);
                resultConsumer.accept(result);
            }
            catch (RemoteException e) {
                e.printStackTrace();
                resultConsumer.accept(Result.Failure.of(e));
            }
        };
        final var thread = new Thread(runnable);

        infoInput.start("Pushing file: " + content.file().path().value());
        thread.start();
    }

    private void onFilePushed(File f) {
        infoInput.end("");

        try {
            AppLocalFiles.removeFromChangeList(f);
            AppLocalFiles.setDownloaded(f);
            Content.updateLocalFs(service);
            filesInput.update();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onFilePushFailed(File.TextFile file) {
        infoInput.setError("Fail to push file: " + file.path().value());
    }

    // ---------- PULL
    private void pullFile(File.TextFile file) {
        final Consumer<Result<TextFileContent>> resultConsumer = result -> {
            if (result instanceof Result.Success<TextFileContent> s) {
                Platform.runLater(() -> onFileContentPulled(s.value()));
            }
            else if (result instanceof Result.Failure<TextFileContent> fail) {
                fail.ifPresent(throwable -> onFilePullFailed(file, throwable.getMessage()));
            }
        };
        final Runnable runnable = () -> {
            try {
                final Result<TextFileContent> result = service.readTextFile(file);
                resultConsumer.accept(result);
            }
            catch (RemoteException e) {
                e.printStackTrace();
                resultConsumer.accept(Result.Failure.of(e));
            }
        };
        final var thread = new Thread(runnable);

        infoInput.start("Pulling file: " + file.path().value());
        thread.start();
    }

    private void onFileContentPulled(TextFileContent content) {
        final File file = content.file();

        updateLocalContent(content);
        infoInput.end("");
        try {
            AppLocalFiles.removeFromChangeList(file);
            AppLocalFiles.setDownloaded(file);
            Content.updateLocalFs(service);

            filesInput.update();
            editorInput.update();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onFilePullFailed(File.TextFile file, String message) {
        infoInput.setError("Fail to pull file: " + file.path().value() + ", " + message);
    }

    private void updateLocalContent(TextFileContent content) {
        Result<Nothing> result;

        if (repository.exists(content.file())) {
            result = repository.set(content);
        }
        else {
            result = repository.add(content);
        }
        if (result instanceof Result.Failure<Nothing> fail) {
            fail.ifPresent(System.out::println);
        }
    }
}
