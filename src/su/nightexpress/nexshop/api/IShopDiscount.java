package su.nightexpress.nexshop.api;

public interface IShopDiscount extends ITimed {

    double getDiscount();

    double getDiscountRaw();

    void setDiscount(double discount);
}
