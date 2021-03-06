package WolfShotz.Wyrmroost.registry;

import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.ReplaceBlockConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

public class WRWorld
{
//    public static final DeferredRegister<Feature<?>> FEATURES = new DeferredRegister<>(ForgeRegistries.FEATURES, Wyrmroost.MOD_ID);

//    public static final RegistryObject<Feature<NoFeatureConfig>> CANARI_TREE_FEATURE = feature("canari_tree_structure", () -> new TreeFeature(NoFeatureConfig::deserialize, true, 3, WRBlocks.CANARI_LOG.get().getDefaultState(), WRBlocks.CANARI_LEAVES.get().getDefaultState(), false));

//    public static final RegistryObject<Structure<NoFeatureConfig>> CANARI_TREE_STRUCTURE = feature("canari_tree", CanariTreeStructure::new);

    // Structure Pieces are not a forge registry, so we don't have to be IMC slave bitches and register on vanilla accord:
//    public static final IStructurePieceType CANARI_TREE_PIECE = structurePiece("canari_tree", CanariTreePiece::new);

    public static void setupWorld()
    {
        for (Biome biome : ForgeRegistries.BIOMES)
        {
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)) // nether features
            {
                oreFeature(biome, Feature.ORE, new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NETHERRACK, WRBlocks.RED_GEODE_ORE.get().getDefaultState(), 4), Placement.COUNT_RANGE, new CountRangeConfig(3, 0, 0, 256));
                continue;
            }

            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)) // end features
            {
                oreFeature(biome, Feature.EMERALD_ORE, new ReplaceBlockConfig(Blocks.END_STONE.getDefaultState(), WRBlocks.PURPLE_GEODE_ORE.get().getDefaultState()), Placement.RANDOM_COUNT_RANGE, new CountRangeConfig(1, 0, 0, 256));
                return;
            }

            // overworld features
            oreFeature(biome, Feature.ORE, new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, WRBlocks.BLUE_GEODE_ORE.get().getDefaultState(), 10), Placement.COUNT_RANGE, new CountRangeConfig(4, 0, 0, 14));
            oreFeature(biome, Feature.ORE, new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, WRBlocks.BLUE_GEODE_ORE.get().getDefaultState(), 9), Placement.COUNT_RANGE, new CountRangeConfig(15, 0, 0, 56));
        }
    }

    /**
     * Helper method for ore features
     * A common placement for ores is using the {@link CountRangeConfig} which has parameters:
     * count: the amount of veins in a chunk
     * bottomOffset: do not generate below this y-level
     * topOffset: really no point to this.. its just subtracted from maximum
     * maximum: do not generate above this y-level
     */
    private static <T extends IFeatureConfig, P extends IPlacementConfig> void oreFeature(Biome biome, Feature<T> feature, T featureConfig, Placement<P> placement, P placeConfig)
    {
        biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                feature.withConfiguration(featureConfig).withPlacement(placement.configure(placeConfig)));
    }
}
