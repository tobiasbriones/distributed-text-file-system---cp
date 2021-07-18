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

package io.github.tobiasbriones.cp.rmifilesystem.client.info;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * @author Tobias Briones
 */
final class InfoView extends VBox implements Info.View {
    private final ProgressIndicator indicator;
    private final Label label;
    private final Label errorLabel;

    InfoView() {
        super();
        indicator = new ProgressIndicator();
        label = new Label();
        errorLabel = new Label();
    }

    @Override
    public Node getView() {
        return this;
    }

    @Override
    public void createView() {
        final var loadingBox = new HBox();

        loadingBox.setAlignment(Pos.CENTER_LEFT);
        loadingBox.setSpacing(16);
        loadingBox.getChildren().addAll(indicator, label);

        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: #E64A19;");

        setPadding(new Insets(4, 16, 16, 16));
        setSpacing(4);
        getChildren().addAll(loadingBox, errorLabel);
    }

    @Override
    public void setError(String value) {
        end("");
        errorLabel.setVisible(true);
        errorLabel.setText(value);
    }

    @Override
    public void start(String value) {
        errorLabel.setVisible(false);
        label.setText(value);
        indicator.setProgress(-1);
    }

    @Override
    public void end(String value) {
        errorLabel.setVisible(false);
        label.setText(value);
        indicator.setProgress(1);
    }
}