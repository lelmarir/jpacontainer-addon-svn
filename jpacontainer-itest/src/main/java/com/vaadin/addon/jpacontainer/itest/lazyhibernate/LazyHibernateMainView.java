package com.vaadin.addon.jpacontainer.itest.lazyhibernate;

import java.util.Set;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.FieldFactory;
import com.vaadin.addon.jpacontainer.itest.lazyhibernate.domain.LazyPerson;
import com.vaadin.addon.jpacontainer.itest.lazyhibernate.domain.LazySkill;
import com.vaadin.addon.jpacontainer.util.HibernateLazyLoadingDelegate;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

public class LazyHibernateMainView extends VerticalLayout implements
        ComponentContainer {

    private JPAContainer<LazyPerson> personContainer;

    public LazyHibernateMainView() {
        final Table table = new Table();

        LazyHibernateEntityManagerProvider entityManagerProvider = new LazyHibernateEntityManagerProvider();
        personContainer = JPAContainerFactory.make(LazyPerson.class,
                "lazyhibernate");
        personContainer.getEntityProvider().setEntityManager(null);
        personContainer.getEntityProvider().setEntityManagerProvider(
                entityManagerProvider);
        personContainer.getEntityProvider().setLazyLoadingDelegate(
                new HibernateLazyLoadingDelegate());

        table.setContainerDataSource(personContainer);
        table.addGeneratedColumn("skillsString", new ColumnGenerator() {
            @SuppressWarnings("unchecked")
            @Override
            public Object generateCell(Table source, Object itemId,
                    Object columnId) {
                String str = "";
                for (LazySkill s : (Set<LazySkill>) personContainer
                        .getItem(itemId).getItemProperty("skills").getValue()) {
                    str += s.getName() + " ";
                }
                return str;
            }
        });
        table.setVisibleColumns(new Object[] { "id", "firstName", "lastName",
                "skillsString", "manager" });

        addComponent(table);

        final Form form = new Form();
        // form.setWriteThrough(false);
        form.setFormFieldFactory(new FieldFactory());
        addComponent(form);
        addComponent(new Button("Save", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                form.commit();
            }
        }));

        table.setSelectable(true);
        table.setImmediate(true);
        table.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                form.setItemDataSource(table.getItem(table.getValue()));
            }
        });

    }
}
