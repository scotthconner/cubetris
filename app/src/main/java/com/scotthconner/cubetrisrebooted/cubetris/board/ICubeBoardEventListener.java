package com.scotthconner.cubetrisrebooted.cubetris.board;

/**
 *
 * Allows an object to become aware about cube board events. Mostly used
 * for generating player experiences around board events, and coordinating across
 * experiences. An abstracted way of processing board messages to reduce coupling
 * between board logic and gameplay experience.
 *
 * Created by scottc on 2/11/16.
 */
public interface ICubeBoardEventListener {
    public void onBoardRotate(CubeBoard cubeBoard);
    public void onBoardRotateBlock(CubeBoard cubeBoard);

    public void onPieceDrop(CubeBoard cubeBoard);
    public void onPieceSlide(CubeBoard cubeBoard);
    public void onPieceHurry(CubeBoard cubeBoard);
    public void onPieceMove(CubeBoard cubeBoard);
    public void onPieceRotate(CubeBoard cubeBoard);
    public void onPieceCommit(CubeBoard cubeBoard);

    public void onLineComplete(CubeBoard cubeBoard);
}
