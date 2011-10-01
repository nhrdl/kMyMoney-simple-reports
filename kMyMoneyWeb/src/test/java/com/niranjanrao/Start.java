package com.niranjanrao;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.xml.sax.SAXException;

import com.niranjanrao.orientdb.OrientDAL;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class Start {
	public static void main(final String[] args) throws Exception {
		final int timeout = (int) Duration.ONE_HOUR.getMilliseconds();

		final CommandLineParser parser = new BasicParser();
		final Options options = new Options();
		options.addOption("h", "help", false, "Print this usage information");
		options.addOption("p", "port", false, "Server port");
		options.addOption("f", "file", true, "File to save program output to");
		// Parse the program arguments
		final CommandLine commandLine = parser.parse(options, args);
		String file = "/home/niranjan/.kmymoney/niranjan.kmy";

		if (commandLine.hasOption('h')) {
			System.out.println("Help Message");
			System.exit(0);
		}
		if (commandLine.hasOption('v')) {
		}
		if (commandLine.hasOption('f')) {
			file = commandLine.getOptionValue('f');
		}

		createDB(file);

		final Server server = new Server();
		final SocketConnector connector = new SocketConnector();

		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(timeout);
		connector.setSoLingerTime(-1);
		connector.setPort(8080);
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
		bb.setWar("src/main/webapp");

		// START JMX SERVER
		// MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		// server.getContainer().addEventListener(mBeanContainer);
		// mBeanContainer.start();

		server.setHandler(bb);

		try {
			System.out
					.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
			server.start();
			System.in.read();
			System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
			server.stop();
			server.join();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void createDB(final String inputSrc)
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
