package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.entities.Item;

public class ItemDao extends JpaDao<Item> {
	
	public ItemDao(EntityManagerFactory entityManagerFactory) {
		super(Item.class, entityManagerFactory);
	}
	
	public List<Item> getItemsByCategoryId(int categoryId) {
		TypedQuery<Item> query = createEntityManager().createQuery(
				"SELECT item FROM " + getTypeParameter().getSimpleName() + " item LEFT JOIN FETCH item.itemSize WHERE item.itemCategory.id = :categoryId", Item.class);
		query.setParameter("categoryId", categoryId);
		return query.getResultList();
	}

	public Item getItemByItemNumber(String itemNumber) {
		TypedQuery<Item> query = createEntityManager().createQuery(
				"SELECT item from " + getTypeParameter().getSimpleName() + " item LEFT JOIN FETCH item.itemSize WHERE item.itemNumber = :itemNumber", Item.class);
		query.setParameter("itemNumber", itemNumber);
		return query.getSingleResult();
	}
	
}
