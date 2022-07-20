package de.hirola.adroles.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Login | AD-Roles")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

	private final LoginForm login = new LoginForm();

	public LoginView(){
		addClassName("login-view");
		setSizeFull();
		setAlignItems(Alignment.CENTER); 
		setJustifyContentMode(JustifyContentMode.CENTER);

		login.setI18n(createLoginI18n());
		login.setAction("login");  

		add(new H1(getTranslation("app.name")), login);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		// inform the user about an authentication error
		if(beforeEnterEvent.getLocation() 
        .getQueryParameters()
        .getParameters()
        .containsKey("error")) {
            login.setError(true);
        }
	}

	private LoginI18n createLoginI18n() {
		LoginI18n i18n = LoginI18n.createDefault();

		LoginI18n.Form i18nForm = i18n.getForm();
		i18nForm.setTitle(getTranslation("login"));
		i18nForm.setUsername(getTranslation("username"));
		i18nForm.setPassword(getTranslation("password"));
		i18nForm.setSubmit(getTranslation("login"));
		i18nForm.setForgotPassword(getTranslation("forgetPassword"));
		i18n.setForm(i18nForm);

		LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
		i18nErrorMessage.setTitle(getTranslation("login.error.title"));
		i18nErrorMessage.setMessage(getTranslation("login.error.message"));
		i18n.setErrorMessage(i18nErrorMessage);

		return i18n;
	}
}