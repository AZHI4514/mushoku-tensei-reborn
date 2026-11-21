package com.azhi.mushoku.gametest;

import com.azhi.mushoku.Mushoku;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Mushoku.MOD_ID)
@PrefixGameTestTemplate(false)
public class PipelineGameTests {
    @GameTest(template = "gametest_empty")
    public static void placesStone(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        helper.runAfterDelay(1, () -> {
            helper.assertBlockPresent(Blocks.STONE, pos);
            helper.succeed();
        });
    }
}
