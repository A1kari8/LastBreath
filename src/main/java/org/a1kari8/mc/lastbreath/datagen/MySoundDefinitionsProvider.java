package org.a1kari8.mc.lastbreath.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import org.a1kari8.mc.lastbreath.LastBreath;

public class MySoundDefinitionsProvider extends SoundDefinitionsProvider {
    public MySoundDefinitionsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LastBreath.MOD_ID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        add(LastBreath.HEARTBEAT, SoundDefinition.definition()
                .with(
                        sound(LastBreath.MOD_ID+":heartbeat", SoundDefinition.SoundType.SOUND)
                                .volume(1.0f)
                                .pitch(1.0f)
                                .attenuationDistance(16)
                                .preload(true)
                )
                // Sets the subtitle.
                .subtitle("sound.lastbreath.heartbeat")
                // Enables replacing.
                .replace(true)
        );
    }
}