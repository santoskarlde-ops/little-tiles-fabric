package team.creative.littletiles.common.block.mc;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.entity.BESignalConverter;

public class BlockSignalConverter extends BaseEntityBlock {
    
    public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    public BlockSignalConverter() {
        super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
    }
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(HORIZONTAL_FACING, mirror.mirror(state.getValue(HORIZONTAL_FACING)));
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
    }
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        changed(level, pos);
    }
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        changed(level, pos);
    }
    
    public void changed(LevelReader level, BlockPos pos) {
        Optional<BESignalConverter> result = level.getBlockEntity(pos, LittleTilesRegistry.BE_SIGNALCONVERTER_TYPE.get());
        if (result.isEmpty())
            return;
        
        Direction direction = level.getBlockState(pos).getValue(HORIZONTAL_FACING);
        
        if (!level.isClientSide())
            result.get().setPower(getPowerOnSide(level, pos.relative(direction.getOpposite()), direction));
    }
    public boolean isSignalSource(BlockState state) {
        return true;
    }
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (state.getValue(HORIZONTAL_FACING) == side.getOpposite()) {
            Optional<BESignalConverter> result = level.getBlockEntity(pos, LittleTilesRegistry.BE_SIGNALCONVERTER_TYPE.get());
            if (result.isPresent())
                return result.get().getPower();
        }
        return 0;
    }
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return state.getValue(HORIZONTAL_FACING) == direction;
    }
    
    public static int getPowerOnSide(LevelReader level, BlockPos pos, Direction side) {
        BlockState iblockstate = level.getBlockState(pos);
        if (iblockstate.isSignalSource())
            return level.getDirectSignal(pos, side);
        return 0;
    }
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BESignalConverter(pos, state);
    }
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
}
