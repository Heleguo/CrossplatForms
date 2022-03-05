package dev.projectg.crossplatforms.config;

import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collection;

/**
 * Constructs printable, user-friendly text from {@link ConfigurationNode}s
 */
public class PrettyPrinter {

    private final int indentSize;
    private final String indent;

    /**
     * Creates a PrettyPrinter with a default indentation level of 2.
     */
    public PrettyPrinter() {
        this.indentSize = 2;
        this.indent = "  ";
    }

    /**
     * Creates a PrettyPrinter
     * @param indent The indent level to use for indentation.
     */
    public PrettyPrinter(int indent) {
        this.indentSize = indent;
        this.indent = " ".repeat(indentSize);
    }

    /**
     * Get the ConfigurationNode and its children as a viewable text
     * @param node The node to generate text from
     * @param showKey Whether or not to include the key of the {@link ConfigurationNode} in the text.
     * @return The viewable text
     */
    public String pretty(ConfigurationNode node, boolean showKey) {
        StringBuilder builder = new StringBuilder();
        addPretty(builder, node, 0, showKey);
        return builder.toString().trim(); // remove unnecessary newline
    }

    /**
     * Get the ConfigurationNode and its children as viewable text. If the {@link ConfigurationNode} has no parent
     * (its key is null), the key is omitted and its value is printed at the base.
     * @param node The node to generate text from
     * @return The viewable text
     */
    public String pretty(ConfigurationNode node) {
        return pretty(node, node.parent() != null);
    }

    /**
     * @param builder The StringBuilder to add the text to
     * @param node The node to generate text from
     * @param indent The indent level to print at
     * @param showKey Whether or not to include the key of the node.
     */
    private void addPretty(StringBuilder builder, ConfigurationNode node, int indent, boolean showKey) {
        String prefix;
        if (showKey) {
            prefix = node.key() + ": ";
        } else {
            prefix = "";
        }

        if (node.virtual()) {
            // node essentially does not exist, show no value
            builder.append(prefix).append("\n");
        } else if (node.isMap()) {
            addCollection(builder, node, node.childrenMap().values(), indent, showKey);
        } else if (node.isList()) {
            addCollection(builder, node, node.childrenList(), indent, showKey);
        } else {
            // show value or blank if raw is null
            builder.append(prefix).append(fromRaw(node.raw())).append("\n");
        }
    }

    /**
     * Generate text from a node and its children (for map-like or list-like nodes)
     * @param builder The StringBuilder to add the text to
     * @param node The node to generate text from
     * @param children The children of the node
     * @param indent The indent level to print at
     * @param key Whether or not to include the key of the node in the text. If the key is included, it is included at
     *            the provided indentation level, and the children are listed a level lower. If not, the children are
     *            directly listed at the given indentation level.
     */
    private void addCollection(StringBuilder builder, ConfigurationNode node, Collection<? extends ConfigurationNode> children, int indent, boolean key) {
        int childIndent;
        if (key) {
            childIndent = indent + 1;
            // indentation for this has already been handled when adding indentation before each child
            builder.append(node.key()).append(":\n");
        } else {
            // no key, so indent children at the given level
            childIndent = indent;
        }

        for (ConfigurationNode child : children) {
            // recurse and add each child
            builder.append(indent(childIndent));
            addPretty(builder, child, childIndent, true);
        }
    }

    private String indent(int level) {
        return indent.repeat(level);
    }

    private static String fromRaw(Object raw) {
        if (raw == null) {
            return "";
        } else {
            return raw.toString();
        }
    }
}
