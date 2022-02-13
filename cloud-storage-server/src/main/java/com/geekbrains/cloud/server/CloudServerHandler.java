package com.geekbrains.cloud.server;

import com.geekbrains.cloud.core.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // init client dir
        currentDir = Paths.get("localserver");
        sendList(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case FILE_REQUEST:
                processFileRequest((FileRequest) cloudMessage, ctx);
                break;
            case FILE:
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx);
                break;
            case UPDATE_PATH_REQUEST:
                processUpdatePathRequest((UpdatePathRequest) cloudMessage, ctx);
                break;
            case PATH_UP:
                processPathUp(ctx);
                sendList(ctx);
                break;
            case DELETE:
                processDelete((DeleteMessage) cloudMessage);
                sendList(ctx);
                break;
        }
    }

    private void processFileMessage(FileMessage cloudMessage) throws IOException {
        Files.write(currentDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new FileMessage(path));
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void processUpdatePathRequest(UpdatePathRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path targetDir = currentDir.resolve(cloudMessage.getDirName()).normalize();
        if (Files.isDirectory(targetDir)) {
            currentDir = targetDir;
            ctx.writeAndFlush(new ListMessage(currentDir));
            ctx.writeAndFlush(new PathResponse(currentDir.toString()));
        }
    }

    private void processPathUp(ChannelHandlerContext ctx) {
        currentDir = currentDir.getParent().normalize();
        if (currentDir != null) {
            ctx.writeAndFlush(new PathResponse(currentDir.toString()));
        }
    }

    private void processDelete(DeleteMessage cloudMessage) throws IOException {
        Path delFile = currentDir.resolve(cloudMessage.getFileName());
        Files.delete(delFile);
    }
}
