package seedu.address.ui;

import javafx.animation.FadeTransition;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import seedu.address.commons.core.EventsCenter;

import static seedu.address.ui.util.GuiUtil.DEFAULT_FADE_DURATION;
import static seedu.address.ui.util.GuiUtil.OPAQUE;
import static seedu.address.ui.util.GuiUtil.TRANSPARENT;

/**
 * Abstract class for all app view controllers.
 * Forces controllers to implement an init method.
 */
abstract class Controller {
    BorderPane appView;

    abstract void init(Pane root);

    abstract void initAppView();

    void addToRootView(Pane root) {
        root.getChildren().add(appView);
    }

    void registerAsEventHandler(Object handler) {
        EventsCenter.getInstance().registerHandler(handler);
    }


    void show() {
        assert appView != null;
        appView.toFront();
        getFadeTransition(DEFAULT_FADE_DURATION, OPAQUE).play();
    }

    void hide() {
        assert appView != null;
        getFadeTransition(DEFAULT_FADE_DURATION, TRANSPARENT).play();
        appView.toBack();
    }

    private FadeTransition getFadeTransition(double duration, double newValue) {
        FadeTransition transition = new FadeTransition(new Duration(duration), appView);
        transition.setToValue(newValue);
        return transition;
    }
}