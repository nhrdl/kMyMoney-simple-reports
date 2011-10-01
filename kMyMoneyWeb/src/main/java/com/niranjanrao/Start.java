package com.niranjanrao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.xml.sax.SAXException;

import com.niranjanrao.orientdb.OrientDAL;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class Start {
	private static final String WICKET_WEBAPP_CLASS_NAME = WicketApplication.class
			.getName();

	public static void main(final String[] args) throws Exception {
		final int timeout = (int) Duration.ONE_HOUR.getMilliseconds();

		int port = 8080;
		final CommandLineParser parser = new BasicParser();
		final Options options = new Options();
		options.addOption("h", "help", false, "Prints usage information");
		options.addOption("p", "port", true, "Server port");
		options.addOption("v", "version", false, "Version");
		options.addOption("f", "file", true, "kMyMoney file path");
		// Parse the program arguments
		final CommandLine commandLine = parser.parse(options, args);
		boolean isBadArgs = false;
		String file = null;

		if (commandLine.hasOption('h')) {
			isBadArgs = true;
		}
		if (commandLine.hasOption('v')) {
			System.out.println("kMyMoneyScripter version 0.01");
			System.exit(0);
		}
		if (!commandLine.hasOption('f')) {
			isBadArgs = true;
		}
		if (commandLine.hasOption('p')) {
			try {
				System.out.println(commandLine.getOptionValue('p'));
				port = Integer.parseInt(commandLine.getOptionValue('p'));
			} catch (final Exception e) {
				isBadArgs = true;
				System.err.println("Not a proper port number:"
						+ commandLine.getOptionValue('p'));
			}
		}
		if (commandLine.hasOption('f')) {
			file = commandLine.getOptionValue('f');
			if (file == null) {
				isBadArgs = true;
			} else {
				final File f = new File(file);
				if (f.exists() == false) {
					System.err.println("File " + file + " does not exist.");
					isBadArgs = true;
				}
			}
		}

		if (isBadArgs) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("kMymoneyScripter", options);
			System.exit(1);
		}
		final Server server = new Server();
		final SocketConnector connector = new SocketConnector();

		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(timeout);
		connector.setSoLingerTime(-1);
		connector.setPort(port);
		server.addConnector(connector);

		// check if a keystore for a SSL certificate is available, and
		// if so, start a SSL connector on port 8443. By default, the
		// quickstart comes with a Apache Wicket Quickstart Certificate
		// that expires about half way september 2021. Do not use this
		// certificate anywhere important as the passwords are available
		// in the source.

		final Resource keystore = Resource.newClassPathResource("/keystore");
		if (keystore != null && keystore.exists()) {
			connector.setConfidentialPort(8443);

			final SslContextFactory factory = new SslContextFactory();
			factory.setKeyStoreResource(keystore);
			factory.setKeyStorePassword("wicket");
			factory.setTrustStore(keystore);
			factory.setKeyManagerPassword("wicket");
			final SslSocketConnector sslConnector = new SslSocketConnector(
					factory);
			sslConnector.setMaxIdleTime(timeout);
			sslConnector.setPort(8443);
			sslConnector.setAcceptors(4);
			server.addConnector(sslConnector);

			System.out
					.println("SSL access to the quickstart has been enabled on port 8443");
			System.out
					.println("You can access the application using SSL on https://localhost:8443");
			System.out.println();
		}

		final WebAppContext bb = new WebAppContext();
		bb.setServer(server);
		bb.setContextPath("/");

		new WicketFilter();
		final FilterHolder filterHolder = new FilterHolder(WicketFilter.class);
		filterHolder.setInitParameter(
				ContextParamWebApplicationFactory.APP_CLASS_PARAM,
				WICKET_WEBAPP_CLASS_NAME);
		filterHolder.setInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");
		// filterHolder.setInitParameter(param, value)
		bb.addFilter(filterHolder, "/*", 1);

		bb.setWar(Start.class.getClassLoader().getResource("webapp")
				.toExternalForm());
		// START JMX SERVER
		// MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		// server.getContainer().addEventListener(mBeanContainer);
		// mBeanContainer.start();

		server.setHandler(bb);
		createDB(file);

		try {
			System.out
					.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
			server.start();
			System.in.read();
			System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
			server.stop();
			server.join();
			WicketApplication.db.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void createDB(final String inputSrc)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		// TODO Auto-generated method stub
		final String url = "memory:/tmp/moneyScript";
		final ODatabaseDocumentTx db = new ODatabaseDocumentTx(url);

		db.create();
		// db.open("admin", "admin");
		final String CLUSTER_NAME = "test";
		db.addPhysicalCluster(CLUSTER_NAME);
		final GZIPInputStream input = new GZIPInputStream(new FileInputStream(
				inputSrc));
		try {

			final OrientDAL rdr = new OrientDAL();
			rdr.loadOrientDB(db, input);

		} finally {
			input.close();
			db.commit();
			// db.close();
			// db.delete();
		}
		WicketApplication.db = db;
	}
}
