/*
 * Copyright (c) 2021 Tobias Briones. All rights reserved.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * This file is part of Course Project at UNAH-MM545: Distributed Text File
 * System.
 *
 * This source code is licensed under the BSD-3-Clause License found in the
 * LICENSE file in the root directory of this source tree or at
 * https://opensource.org/licenses/BSD-3-Clause.
 */

package io.github.tobiasbriones.cp.rmifilesystem.client.ui.menu;

import io.github.tobiasbriones.cp.rmifilesystem.mvp.Initializable;
import io.github.tobiasbriones.cp.rmifilesystem.mvp.MvpPresenter;
import io.github.tobiasbriones.cp.rmifilesystem.mvp.MvpView;
import javafx.scene.Node;

import java.io.File;

/**
 * @author Tobias Briones
 */
public final class AppMenu implements Initializable {
    public interface Output extends DefaultOutput {
        void onCreateNewFile(File file);

        void onCreateNewDirectory(File dirFile);

        void onLogin(String clientName);
    }

    interface DefaultOutput {
        void onSave();

        void onClose();
    }

    interface Controller extends DefaultOutput {
        void onNewFile();

        void onNewDirectory();

        void onLogin();

        void onQuit();

        void onAbout();
    }

    interface View extends MvpView<Controller> {}

    interface Presenter extends MvpPresenter<Output>, Controller {}

    private final View view;
    private final Presenter presenter;

    public AppMenu() {
        view = new AppMenuView();
        presenter = new AppMenuPresenter(view);
    }

    public Node getView() {
        return view.getView();
    }

    public void setOutput(Output output) {
        presenter.setOutput(output);
    }

    public void init() {
        presenter.init();
    }
}