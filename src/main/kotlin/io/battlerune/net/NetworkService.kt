package io.battlerune.net

import io.battlerune.game.GameContext
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFutureListener
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.apache.logging.log4j.LogManager

class NetworkService(val gameContext: GameContext) {

    companion object {
        val logger = LogManager.getLogger()
    }

    fun start(port: Int) {
        val bossGroup = NioEventLoopGroup(1)
        val loopGroup = NioEventLoopGroup()
        try {
            val sb = ServerBootstrap()

            sb.group(bossGroup, loopGroup)
                    .channel(NioServerSocketChannel().javaClass)
                    .handler(LoggingHandler(LogLevel.INFO))
                    .childHandler(ServerPipelineInitializer(gameContext))

            val f = sb.bind(port).syncUninterruptibly()

            logger.info("BattleRune bound to port $port.")

            f.channel().closeFuture().sync()
        } catch (ex : Exception) {
            ex.printStackTrace()
        } finally {
            bossGroup.shutdownGracefully()
            loopGroup.shutdownGracefully()
        }

    }

    fun portAvailable(port: Int) : Boolean {
        val future = ServerBootstrap().bind(port)

        if (future.isSuccess) {
            future.addListener { ChannelFutureListener.CLOSE }
        }

        return future.isSuccess
    }

}