package trd.home.media.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.*;
import trd.home.media.dto.MediaFile;
import trd.home.media.exception.*;
import trd.home.media.repository.*;

@Service
public class MediaPersistenceService {
    private final ActorRepository actors;
    private final FolderRepository folders;
    private final VideoRepository videos;

    public MediaPersistenceService(ActorRepository actors, FolderRepository folders, VideoRepository videos) {
        this.actors = actors;
        this.folders = folders;
        this.videos = videos;
    }

    @Transactional
    @LogMethodCall
    public int save(List<MediaFile> files) {
        actors.findAll().forEach(actor -> actor.setStatus(MediaStatus.INACTIVE));
        folders.findAll().forEach(folder -> folder.setStatus(MediaStatus.INACTIVE));
        videos.findAll().forEach(video -> video.setStatus(MediaStatus.INACTIVE));
        Set<String> identities = new HashSet<>();
        for (MediaFile file : files) {
            String path = file.path().getParent().toString();
            Folder folder = folders.findByPath(path).orElseGet(() -> {
                Folder created = new Folder();
                created.setPath(path);
                created.setType(file.path().getParent().getFileName().toString());
                return folders.save(created);
            });
            folder.setStatus(MediaStatus.ACTIVE);
            Set<Actor> cast = new HashSet<>();
            for (String name : file.video().actors()) {
                Actor actor = actors.findByName(name).orElseGet(() -> {
                    Actor created = new Actor();
                    created.setName(name);
                    return actors.save(created);
                });
                actor.setStatus(MediaStatus.ACTIVE);
                cast.add(actor);
            }
            String key = actorKey(file.video().actors());
            String identity = folder.getId() + ":" + key + ":" + file.video().name();
            if (!identities.add(identity))
                throw new DuplicateVideoException(
                        "Multiple files have the same folder, actors and video name: " + file.path());
            Video video = videos.findByFolderIdAndNameAndActorKey(
                            folder.getId(), file.video().name(), key)
                    .orElseGet(Video::new);
            video.setName(file.video().name());
            video.setFolder(folder);
            video.setActors(cast);
            video.setActorKey(key);
            video.setFileName(file.path().getFileName().toString());
            video.setStatus(MediaStatus.ACTIVE);
            videos.save(video);
        }
        return files.size();
    }

    static String actorKey(Set<String> names) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            names.stream().sorted().forEach(name -> {
                byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
                digest.update(
                        java.nio.ByteBuffer.allocate(4).putInt(bytes.length).array());
                digest.update(bytes);
            });
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new UnableToGenerateActorKeyException("Unable to generate the video actor key.", exception);
        }
    }
}
