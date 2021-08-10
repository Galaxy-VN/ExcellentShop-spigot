package su.nightexpress.nexshop.shop.virtual.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.regex.RegexUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtual;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class EditorHandlerShop implements IEditorHandler<IShopVirtual> {

    private ExcellentShop plugin;
    private VirtualShop   virtualShop;

    public EditorHandlerShop(@NotNull VirtualShop virtualShop) {
        this.plugin = virtualShop.plugin;
        this.virtualShop = virtualShop;
    }

    @Override
    public boolean onType(
            @NotNull Player player, @Nullable IShopVirtual shop,
            @NotNull VirtualEditorType type, @NotNull String msg) {

        if (shop == null) {
            if (type == VirtualEditorType.CREATE_SHOP) {
                String id = StringUT.colorOff(msg);
                if (!RegexUT.matchesEn(id)) {
                    EditorManager.errorCustom(player, plugin.lang().Virtual_Shop_Editor_Create_Error_BadId.getMsg());
                    return false;
                }

                if (this.virtualShop.getShopById(id) != null) {
                    EditorManager.errorCustom(player, plugin.lang().Virtual_Shop_Editor_Create_Error_Exist.getMsg());
                    return false;
                }
                IShopVirtual shop2 = new ShopVirtual(this.virtualShop, this.virtualShop.getFullPath() + VirtualShop.DIR_SHOPS + id + "/" + id + ".yml");
                this.virtualShop.getShopsMap().put(shop2.getId(), shop2);
                this.virtualShop.getEditorHandler().getEditorShopList().open(player, 1);

            }
            return true;
        }

        switch (type) {
            case CHANGE_TITLE -> {
                shop.getView().setTitle(msg);
            }
            case CHANGE_CITIZENS_ID -> {
                msg = StringUT.colorOff(msg);
                int input = StringUT.getInteger(msg, -1);
                if (input < 0) {
                    EditorManager.errorNumber(player, false);
                    return false;
                }

                List<Integer> current = new ArrayList<>(IntStream.of(shop.getCitizensIds()).boxed().toList());
                if (current.contains(input)) break;
                current.add(input);

                shop.setCitizensIds(current.stream().mapToInt(i -> i).toArray());
            }
            default -> {}
        }

        shop.save();
        shop.getEditor().open(player, 1);

        return true;
    }
}
