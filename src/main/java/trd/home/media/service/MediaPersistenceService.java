package trd.home.media.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.common.logging.LogMethodCall;
import trd.home.media.constant.MediaStatus;
import trd.home.media.dao.Actor;
import trd.home.media.dao.Folder;
import trd.home.media.dao.Video;
import trd.home.media.dto.MediaFile;
import trd.home.media.exception.DuplicateVideoException;
import trd.home.media.exception.UnableToGenerateActorKeyException;
import trd.home.media.repository.ActorRepository;
import trd.home.media.repository.FolderRepository;
import trd.home.media.repository.VideoRepository;

@Service
@RequiredArgsConstructor
public class MediaPersistenceService {
    private final ActorRepository actors;
    private final FolderRepository folders;
    private final VideoRepository videos;

    @Transactional
    @LogMethodCall
    public int save(List<MediaFile> files) {
        deactivateExistingMedia();
        Set<String> identities = new HashSet<>();
        for (MediaFile file : files) {
            Folder folder = findOrCreateFolder(file);
            Set<Actor> cast = findOrCreateActors(file.video().actors());
            String actorKey = actorKey(file.video().actors());
            if (!identities.add(videoIdentity(folder, actorKey, file.video().name()))) {
                throw new DuplicateVideoException(
                        "Multiple files have the same folder, actors and video name: " + file.path());
            }
            saveVideo(file, folder, cast, actorKey);
        }
        return files.size();
    }

    private void deactivateExistingMedia() {
        actors.findAll().forEach(actor -> actor.setStatus(MediaStatus.INACTIVE));
        folders.findAll().forEach(folder -> folder.setStatus(MediaStatus.INACTIVE));
        videos.findAll().forEach(video -> video.setStatus(MediaStatus.INACTIVE));
    }

    private Folder findOrCreateFolder(MediaFile file) {
        String path = file.path().getParent().toString();
        Folder folder = folders.findByPath(path).orElseGet(() -> {
            Folder created = new Folder();
            created.setPath(path);
            created.setType(file.path().getParent().getFileName().toString());
            return folders.save(created);
        });
        folder.setStatus(MediaStatus.ACTIVE);
        return folder;
    }

    private Set<Actor> findOrCreateActors(Set<String> names) {
        Set<Actor> cast = new HashSet<>();
        for (String name : names) {
            Actor actor = actors.findByName(name).orElseGet(() -> {
                Actor created = new Actor();
                created.setName(name);
                return actors.save(created);
            });
            actor.setStatus(MediaStatus.ACTIVE);
            cast.add(actor);
        }
        return cast;
    }

    private void saveVideo(MediaFile file, Folder folder, Set<Actor> cast, String actorKey) {
        Video video = videos.findByFolderIdAndNameAndActorKey(
                        folder.getId(), file.video().name(), actorKey)
                .orElseGet(Video::new);
        video.setName(file.video().name());
        video.setFolder(folder);
        video.setActors(cast);
        video.setActorKey(actorKey);
        video.setFileName(file.path().getFileName().toString());
        video.setStatus(MediaStatus.ACTIVE);
        videos.save(video);
    }

    private static String videoIdentity(Folder folder, String actorKey, String videoName) {
        return folder.getId() + ":" + actorKey + ":" + videoName;
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
