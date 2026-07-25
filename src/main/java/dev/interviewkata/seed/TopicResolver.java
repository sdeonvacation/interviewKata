package dev.interviewkata.seed;

import dev.interviewkata.model.Topic;
import dev.interviewkata.repository.TopicRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves topic paths like "Java Core/Collections" to Topic entities.
 * Caches resolved topics for the duration of the seeding process.
 */
public class TopicResolver {

    private final TopicRepository topicRepository;
    private final Map<String, Topic> cache = new ConcurrentHashMap<>();

    public TopicResolver(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    /**
     * Resolve a topic path to its entity.
     * Path format: "RootName/ChildName" (exactly two segments supported).
     *
     * @param path topic path like "Java Core/Collections"
     * @return resolved Topic entity
     * @throws IllegalArgumentException if path is invalid or topic not found
     */
    public Topic resolve(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Topic path must not be blank");
        }

        return cache.computeIfAbsent(path, this::doResolve);
    }

    private Topic doResolve(String path) {
        String[] segments = path.split("/");
        if (segments.length < 1 || segments.length > 2) {
            throw new IllegalArgumentException(
                    "Topic path must have 1 or 2 segments separated by '/': " + path);
        }

        String rootName = segments[0].trim();
        List<Topic> roots = topicRepository.findByParentIdIsNull();
        Topic root = roots.stream()
                .filter(t -> t.getName().equalsIgnoreCase(rootName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Root topic not found: '" + rootName + "'"));

        if (segments.length == 1) {
            return root;
        }

        String childName = segments[1].trim();
        List<Topic> children = topicRepository.findByParentId(root.getId());
        return children.stream()
                .filter(t -> t.getName().equalsIgnoreCase(childName))
                .findFirst()
                .orElseGet(() -> {
                    // Auto-create missing child topic
                    Topic newChild = Topic.builder()
                            .name(childName)
                            .area(root.getArea())
                            .parent(root)
                            .sortOrder(children.size() + 1)
                            .build();
                    return topicRepository.save(newChild);
                });
    }

    /** Clear the internal cache (useful between test runs). */
    public void clearCache() {
        cache.clear();
    }
}
