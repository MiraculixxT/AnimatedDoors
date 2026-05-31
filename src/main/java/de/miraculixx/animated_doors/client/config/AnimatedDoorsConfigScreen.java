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

    public AnimatedDoorsConfigScreen(Screen parent) {
        super(Component.literal("AnimatedDoors Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int controlsWidth = Math.min(300, width - 40);
        int x = (width - controlsWidth) / 2;
        int y = height / 2 - 52;
        int toggleGap = 6;
        int toggleWidth = (controlsWidth - toggleGap * 2) / 3;

        addRenderableWidget(new DurationSlider(x, y, controlsWidth, 20));
        addRenderableWidget(CycleButton
            .builder(easing -> Component.literal(easing.displayName()), config.easing())
            .withValues(List.of(AnimatedDoorsConfig.Easing.values()))
            .create(x, y + 28, controlsWidth, 20, Component.literal("Easing"), (button, easing) -> {
                config.setEasing(easing);
                config.save();
            })
        );
        addRenderableWidget(toggleButton(
            x,
            y + 56,
            toggleWidth,
            "Doors",
            config::doorsEnabled,
            config::setDoorsEnabled
        ));
        addRenderableWidget(toggleButton(
            x + toggleWidth + toggleGap,
            y + 56,
            toggleWidth,
            "Trapdoors",
            config::trapdoorsEnabled,
            config::setTrapdoorsEnabled
        ));
        addRenderableWidget(toggleButton(
            x + (toggleWidth + toggleGap) * 2,
            y + 56,
            toggleWidth,
            "Gates",
            config::fenceGatesEnabled,
            config::setFenceGatesEnabled
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
        graphics.centeredText(font, title, width / 2, 20, -1);
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
