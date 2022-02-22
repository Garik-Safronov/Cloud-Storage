package com.geekbrains.cloud.server;

import com.geekbrains.cloud.core.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private AuthService authService;
    private Path currentDir;
    private Path userDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        authService = new AuthService();
        authService.start();

        currentDir = Paths.get("localserver");
        sendList(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case AUTH:
                System.out.println("Получено AUTH");
                processAuthMessage((AuthMessage) cloudMessage, ctx);
                break;
            case REG:
                processRegMessage((RegMessage) cloudMessage, ctx);
                break;
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
            case RENAME:
                processRenameFile((RenameMessage) cloudMessage, ctx);
                break;
            case MK_DIR:
                processCreateDir((CreateDirMessage) cloudMessage, ctx);
                break;
            case DELETE:
                processDelete((DeleteMessage) cloudMessage, ctx);
                break;
        }
    }

    private void processAuthMessage(AuthMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        String nickname = authService.getNickByLoginAndPass(cloudMessage.getLogin(), cloudMessage.getPassword());
        if (nickname != null) {
            ctx.writeAndFlush(new AuthConfirm());
            log.debug("SERVER: успешная авторизация пользователя");
            userDir = Paths.get(nickname);
            ctx.writeAndFlush(new ListMessage(userDir));
        } else {
            ctx.writeAndFlush(new AuthError());
            log.error("SERVER: ошибка авторизации пользователя " + cloudMessage.getLogin());
        }
    }

    private void processRegMessage(RegMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        authService.addNewClient(cloudMessage.getLogin(), cloudMessage.getPassword(), cloudMessage.getNickname());
        if (!authService.isNicknameUsed(cloudMessage.getNickname())) {
            ctx.writeAndFlush(new RegConfirm());
            log.debug("SERVER: успешная авторизация пользователя");
            userDir = Files.createDirectory(Paths.get(currentDir.toString() + "/" + cloudMessage.getNickname()));
        } else {
            ctx.writeAndFlush(new RegError());
            log.error("SERVER: ошибка регистрации пользователя " + cloudMessage.getNickname());
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
        System.out.println("Отправлено LIST");
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

    private void processRenameFile(RenameMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path fileOldName = Paths.get(cloudMessage.getOldFileName());
        Files.move(fileOldName, fileOldName.resolveSibling(cloudMessage.getNewFileName()));
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void processCreateDir(CreateDirMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Files.createDirectory(Paths.get(currentDir.toString() + "/" + cloudMessage.getNewDirName()));
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void processDelete(DeleteMessage cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path delFile = currentDir.resolve(cloudMessage.getFileName());
        Files.delete(delFile);
        ctx.writeAndFlush(new ListMessage(currentDir));
    }
}
