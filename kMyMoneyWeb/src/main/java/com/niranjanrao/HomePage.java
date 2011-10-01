package com.niranjanrao;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {
		add(new Label("version", getApplication().getFrameworkSettings()
				.getVersion()));
	}

	@Override
	public void renderHead(final IHeaderResponse resp) {
		resp.renderJavaScriptReference(new PackageResourceReference(
				HomePage.class, "js/test.js"));
		super.renderHead(resp);
	}
}
