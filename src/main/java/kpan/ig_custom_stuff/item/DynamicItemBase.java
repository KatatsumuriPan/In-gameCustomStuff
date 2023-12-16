package kpan.ig_custom_stuff.item;

import kpan.ig_custom_stuff.resource.ids.ItemId;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.item.Item;

public class DynamicItemBase extends Item {

	public DynamicItemBase(ItemId itemId, ItemPropertyEntry itemPropertyEntry) {

		setTranslationKey(itemId.namespace + "." + itemId.name);
		MyReflectionHelper.setPrivateField(Impl.class, this, "registryName", itemId.toResourceLocation());
		//setHasSubtypes(true)/*ダメージ値等で複数の種類のアイテムを分けているかどうか。デフォルトfalse*/
		//setMaxDamage(256)/*耐久値の設定。デフォルト0*/
		//setFull3D()/*3D表示で描画させる。ツールや骨、棒等。*/
		//setContainerItem(Items.stick)/*クラフト時にアイテムを返却できるようにしている際の返却アイテムの指定。*/
		//setPotionEffect(PotionHelper.ghastTearEffect)/*指定文字列に対応した素材として醸造台で使える。PotionHelper参照のこと。*/
		//setNoRepair()/*修理レシピを削除し、金床での修繕を出来なくする*/

		setProperty(itemPropertyEntry);
	}

	public void setProperty(ItemPropertyEntry itemPropertyEntry) {
		setCreativeTab(itemPropertyEntry.creativeTab);
	}

}
