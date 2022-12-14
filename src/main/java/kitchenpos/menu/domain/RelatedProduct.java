package kitchenpos.menu.domain;

import static javax.persistence.FetchType.EAGER;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import kitchenpos.product.domain.Product;

@Embeddable
public class RelatedProduct {

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private long quantity;

    protected RelatedProduct() {
    }

    public RelatedProduct(final Product product, final long quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public long getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }
}
