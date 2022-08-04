package de.hirola.adroles.views.adgroups;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.ADGroup;

public class ADGroupForm extends FormLayout {
  private ADGroup adGroup;
  private  final Binder<ADGroup> binder = new BeanValidationBinder<>(ADGroup.class);
  private VerticalLayout formsLayout = new VerticalLayout();
  private final TextField name = new TextField(getTranslation("name"));
  private final TextField distinguishedName = new TextField(getTranslation("distinguishedName"));
  private final TextField description = new TextField(getTranslation("description"));
  private final RadioButtonGroup<String> groupAreaRadioGroup = new RadioButtonGroup<>();
  private final RadioButtonGroup<String> groupTypeRadioGroup = new RadioButtonGroup<>();
  private final Checkbox isAdminGroup = new Checkbox(getTranslation("adminGroup"));
  private Button saveButton;

  public ADGroupForm() {
    addClassName("ad-group-form");
    setResponsiveSteps(new ResponsiveStep("500px", 1));
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of a person
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    name.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    distinguishedName.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    description.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);

    groupAreaRadioGroup.setLabel(getTranslation("groupArea"));
    groupAreaRadioGroup.setItems(getTranslation("local"), getTranslation("global"),
            getTranslation("universal"));

    groupTypeRadioGroup.setLabel(getTranslation("groupType"));
    groupTypeRadioGroup.setItems(getTranslation("security"), getTranslation("distribution"));

    formsLayout = new VerticalLayout(name, distinguishedName, description, isAdminGroup);
    formsLayout.setPadding(true);

    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button deleteButton = new Button(getTranslation("delete"));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    deleteButton.addClickListener(event -> fireEvent(new DeleteEvent(this, adGroup)));

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout_1 = new HorizontalLayout(saveButton, deleteButton, closeButton);
    buttonsLayout_1.setPadding(true);

    add(formsLayout, buttonsLayout_1);
  }

  public void setAdGroup(ADGroup adGroup) {
    this.adGroup = adGroup;
    binder.readBean(adGroup);
    // workaround: boolean not (correct) bound as instance field
    if (adGroup != null) {
      isAdminGroup.setValue(adGroup.isAdminGroup());

      switch (adGroup.getGroupArea()) {
        case Global.ADGroupArea.GLOBAL -> {
          groupAreaRadioGroup.setValue(getTranslation("global"));
          formsLayout.add(groupAreaRadioGroup);
        }
        case Global.ADGroupArea.UNIVERSAL -> {
          groupAreaRadioGroup.setValue(getTranslation("universal"));
          formsLayout.add(groupAreaRadioGroup);
        }
        default -> {
          groupAreaRadioGroup.setValue(getTranslation("local"));
          formsLayout.add(groupAreaRadioGroup);
        }
      }

      if (adGroup.getGroupType() == Global.ADGroupType.DISTRIBUTION) {
        groupTypeRadioGroup.setValue(getTranslation("distribution"));
        formsLayout.add(groupTypeRadioGroup);
      } else {
        groupTypeRadioGroup.setValue(getTranslation("security"));
        formsLayout.add(groupTypeRadioGroup);
      }
    } else {
      isAdminGroup.setValue(false);
      groupAreaRadioGroup.setValue(getTranslation("local"));
      groupTypeRadioGroup.setValue(getTranslation("security"));
      formsLayout.add(groupAreaRadioGroup, groupTypeRadioGroup);
    }
  }

  private void validateAndSave() {
    try {
      // workaround: boolean not (correct) bound as instance field
      adGroup.setAdminGroup(isAdminGroup.getValue());
      
      String groupAreaString = groupAreaRadioGroup.getValue();
      if (groupAreaString.equalsIgnoreCase(getTranslation("global"))) {
        adGroup.setGroupArea(Global.ADGroupArea.GLOBAL);
      } else if (groupAreaString.equalsIgnoreCase(getTranslation("universal"))) {
        adGroup.setGroupArea(Global.ADGroupArea.UNIVERSAL);
      } else {
        adGroup.setGroupArea(Global.ADGroupArea.LOCAL);
      }
      
      String groupTypeString = groupTypeRadioGroup.getValue();
      if (groupTypeString.equalsIgnoreCase(getTranslation("security"))) {
        adGroup.setGroupType(Global.ADGroupType.SECURITY);
      } else {
        adGroup.setGroupType(Global.ADGroupType.DISTRIBUTION);
      }
      
      binder.writeBean(adGroup);
      fireEvent(new SaveEvent(this, adGroup));
    } catch (ValidationException exception) {
      exception.printStackTrace();
    }
  }

  // Events
  public static abstract class ADUserFormEvent extends ComponentEvent<ADGroupForm> {
    private final ADGroup adGroup;

    protected ADUserFormEvent(ADGroupForm source, ADGroup adGroup) {
      super(source, false);
      this.adGroup = adGroup;
    }

    public ADGroup getAdGroup() {
      return adGroup;
    }
  }

  public static class SaveEvent extends ADUserFormEvent {
    SaveEvent(ADGroupForm source, ADGroup adGroup) {
      super(source, adGroup);
    }
  }

  public static class DeleteEvent extends ADUserFormEvent {
    DeleteEvent(ADGroupForm source, ADGroup adGroup) {
      super(source, adGroup);
    }

  }

  public static class CloseEvent extends ADUserFormEvent {
    CloseEvent(ADGroupForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}