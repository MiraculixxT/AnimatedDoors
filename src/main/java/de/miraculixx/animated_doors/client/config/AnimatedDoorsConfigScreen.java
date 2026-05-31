package de.miraculixx.animated_doors.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class AnimatedDoorsConfigScreen extends Screen {
    private final Screen parent;
    private final AnimatedDoorsConfig config = AnimatedDoorsConfig.instance();
    private int animationTitleY;
    private int linkingTitleY;

    public AnimatedDoorsConfigScreen(Screen parent) {
        super(Component.literal("AnimatedDoors Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int controlsWidth = Math.min(300, width - 40);
        int x = (width - controlsWidth) / 2;
        int animationY = height / 2 - 82;
        animationTitleY = animationY - 15;
        linkingTitleY = animationY + 90;
        int linkingY = linkingTitleY + 16;
        int toggleGap = 6;
        int toggleWidth = (controlsWidth - toggleGap * 2) / 3;
        int widgetGap = 24;

        addRenderableWidget(new DurationSlider(x, animationY, controlsWidth, 20));
        addRenderableWidget(CycleButton
            .builder(easing -> Component.literal(easing.displayName()), config.easing())
            .withValues(List.of(AnimatedDoorsConfig.Easing.values()))
            .create(x, animationY + widgetGap, controlsWidth, 20, Component.literal("Easing"), (button, easing) -> {
                config.setEasing(easing);
                config.save();
            })
        );
        addRenderableWidget(toggleButton(
            x,
            animationY + widgetGap*2,
            toggleWidth,
            "Doors",
            config::doorsEnabled,
            config::setDoorsEnabled
        ));
        addRenderableWidget(toggleButton(
            x + toggleWidth + toggleGap,
            animationY + widgetGap*2,
            toggleWidth,
            "Trapdoors",
            config::trapdoorsEnabled,
            config::setTrapdoorsEnabled
        ));
        addRenderableWidget(toggleButton(
            x + (toggleWidth + toggleGap) * 2,
            animationY + widgetGap*2,
            toggleWidth,
            "Gates",
            config::fenceGatesEnabled,
            config::setFenceGatesEnabled
        ));

        addRenderableWidget(toggleButton(
                x,
                linkingY,
                controlsWidth,
                "Activate on Servers",
                config::connectedBlocksOnServersEnabled,
                config::setConnectedBlocksOnServersEnabled
        ));
        addRenderableWidget(toggleButton(
            x,
            linkingY + widgetGap,
            toggleWidth,
            "Doors Link",
            config::connectedDoorsEnabled,
            config::setConnectedDoorsEnabled
        ));
        addRenderableWidget(toggleButton(
            x + toggleWidth + toggleGap,
            linkingY + widgetGap,
            toggleWidth,
            "Traps Link",
            config::connectedTrapdoorsEnabled,
            config::setConnectedTrapdoorsEnabled
        ));
        addRenderableWidget(toggleButton(
            x + (toggleWidth + toggleGap) * 2,
            linkingY + widgetGap,
            toggleWidth,
            "Gates Link",
            config::connectedFenceGatesEnabled,
            config::setConnectedFenceGatesEnabled
        ));
        addRenderableWidget(Button
            .builder(Component.literal("Reset"), button -> {
                config.reset();
                config.save();
                clearWidgets();
                init();
            })
            .bounds(width / 2 - 104, height - 32, 100, 20)
            .build()
        );
        addRenderableWidget(Button
            .builder(Component.literal("Done"), button -> onClose())
            .bounds(width / 2 + 4, height - 32, 100, 20)
            .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 10, -1);
        graphics.centeredText(font, Component.literal("Animation Settings"), width / 2, animationTitleY, -1);
        graphics.centeredText(font, Component.literal("Linking Settings"), width / 2, linkingTitleY, -1);
    }

    @Override
    public void onClose() {
        config.save();
        Minecraft.getInstance().setScreen(parent);
    }

    private Button toggleButton(int x, int y, int width, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        return Button
            .builder(toggleLabel(label, getter.getAsBoolean()), button -> {
                boolean enabled = !getter.getAsBoolean();
                setter.accept(enabled);
                config.save();
                button.setMessage(toggleLabel(label, enabled));
            })
            .bounds(x, y, width, 20)
            .build();
    }

    private static Component toggleLabel(String label, boolean enabled) {
        return Component.literal(label + ": " + (enabled ? "On" : "Off"));
    }

    private final class DurationSlider extends AbstractSliderButton {
        private DurationSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), toSliderValue(AnimatedDoorsConfig.instance().durationSeconds()));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(String.format(Locale.ROOT, "Animation Speed: %.1fs", toDurationSeconds(value))));
        }

        @Override
        protected void applyValue() {
            config.setDurationSeconds(toDurationSeconds(value));
            config.save();
        }

        private static double toSliderValue(float durationSeconds) {
            float range = AnimatedDoorsConfig.MAX_DURATION_SECONDS - AnimatedDoorsConfig.MIN_DURATION_SECONDS;
            return range <= 0.0f ? 0.0 : (durationSeconds - AnimatedDoorsConfig.MIN_DURATION_SECONDS) / range;
        }

        private float toDurationSeconds(double value) {
            float range = AnimatedDoorsConfig.MAX_DURATION_SECONDS - AnimatedDoorsConfig.MIN_DURATION_SECONDS;
            float duration = AnimatedDoorsConfig.MIN_DURATION_SECONDS + (float) value * range;
            return Math.round(duration * 10.0f) / 10.0f;
        }
    }
}
