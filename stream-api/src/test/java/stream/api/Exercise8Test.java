package stream.api;

import common.test.tool.annotation.Difficult;
import common.test.tool.dataset.ClassicOnlineStore;
import common.test.tool.entity.Customer;
import common.test.tool.entity.Item;
import common.test.tool.entity.Shop;

import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class Exercise8Test extends ClassicOnlineStore {

    @Difficult @Test
    public void itemsNotOnSale() {
        Stream<Customer> customerStream = this.mall.getCustomerList().stream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a set of item names that are in {@link Customer.wantToBuy} but not on sale in any shop.
         */
        Stream<Item> itemsOnSaleStream = shopStream.flatMap(s -> s.getItemList().stream());
        Stream<Item> itemsWantToBuyStream = customerStream.flatMap(c->c.getWantToBuy().stream());
        Set<String> itemListOnSaleStrings = itemsOnSaleStream.map(Item::getName).collect(Collectors.toSet());
        Set<String> itemSetNotOnSale = itemsWantToBuyStream.map(Item::getName).filter(s -> !itemListOnSaleStrings.contains(s)).collect(Collectors.toSet());
        //  .filter(itemName -> itemListOnSale.stream().noneMatch(itemName::equals)) <- Could also be (probably better performance?)
        assertThat(itemSetNotOnSale, hasSize(3));
        assertThat(itemSetNotOnSale, hasItems("bag", "pants", "coat"));
    }

    @Difficult @Test
    public void havingEnoughMoney() {
        Stream<Customer> customerStream = this.mall.getCustomerList().stream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a customer's name list including who are having enough money to buy all items they want which is on sale.
         * Items that are not on sale can be counted as 0 money cost.
         * If there is several same items with different prices, customer can choose the cheapest one.
         */
        // Sum all "baskets prices"
        // See that it's lower than budget
        List<Item> onSale = shopStream.flatMap(s -> s.getItemList().stream()).sorted(Comparator.comparing(Item::getPrice)).collect(Collectors.toList());
        // now we have sorted list of items with ascending to price so the first is the lowest
        Predicate<Customer> havingEnoughMoney = c ->
            c.getBudget() >=
            c.getWantToBuy().stream().mapToInt(wi -> onSale.stream()
                    .filter(si -> si.getName().equalsIgnoreCase(wi.getName()))
                    .findFirst().map(Item::getPrice).orElse(0))
                    .sum();

        List<String> customerNameList = customerStream.filter(havingEnoughMoney).map(Customer::getName).collect(Collectors.toList());

        assertThat(customerNameList, hasSize(7));
        assertThat(customerNameList, hasItems("Joe", "Patrick", "Chris", "Kathy", "Alice", "Andrew", "Amy"));
    }
}
