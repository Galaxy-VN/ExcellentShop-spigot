package su.nightexpress.nexshop.api.editor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEditorHandler<T, K extends Enum<K>> {

    boolean onType(@NotNull Player player, @Nullable T obj, @NotNull K type, @NotNull String msg);
}
