/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.libglom.Field;
import org.jooq.InsertResultStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.Factory;

import com.google.common.io.Files;
import com.ibm.icu.text.NumberFormat;

/**
 * @author Murray Cumming <murrayc@murrayc.com>
 * 
 */
public class SelfHoster {
	// private String tempFilepathDir = "";
	private boolean selfHostingActive = false;
	private Document document = null;
	private String username = "";
	private String password = "";

	SelfHoster(final Document document) {
		this.document = document;
	}

	private static final int PORT_POSTGRESQL_SELF_HOSTED_START = 5433;
	private static final int PORT_POSTGRESQL_SELF_HOSTED_END = 5500;

	private static final String DEFAULT_CONFIG_PG_HBA_LOCAL_8p4 = "# TYPE  DATABASE    USER        CIDR-ADDRESS          METHOD\n"
			+ "\n"
			+ "# local is for Unix domain socket connections only\n"
			+ "# trust allows connection from the current PC without a password:\n"
			+ "local   all         all                               trust\n"
			+ "local   all         all                               ident\n"
			+ "local   all         all                               md5\n"
			+ "\n"
			+ "# TCP connections from the same computer, with a password:\n"
			+ "host    all         all         127.0.0.1    255.255.255.255    md5\n"
			+ "# IPv6 local connections:\n"
			+ "host    all         all         ::1/128               md5\n";

	private static final String DEFAULT_CONFIG_PG_IDENT = "";
	private static final String FILENAME_DATA = "data";

	public boolean createAndSelfHostFromExample(final Document.HostingMode hostingMode) {

		if (!createAndSelfHostNewEmpty(hostingMode)) {
			// std::cerr << G_STRFUNC << ": test_create_and_selfhost_new_empty() failed." << std::endl;
			return false;
		}

		final boolean recreated = recreateDatabaseFromDocument(); /* TODO: Progress callback */
		if (!recreated) {
			cleanup();
		}

		return recreated;
	}

	/**
	 * @param document
	 * @param
	 * @param subDirectoryPath
	 * @return
	 */
	private boolean createAndSelfHostNewEmpty(Document.HostingMode hostingMode) {
		if (hostingMode != Document.HostingMode.HOSTING_MODE_POSTGRES_SELF) {
			// TODO: std::cerr << G_STRFUNC << ": This test function does not support the specified hosting_mode: " <<
			// hosting_mode << std::endl;
			return false;
		}

		// Save a copy, specifying the path to file in a directory:
		// For instance, /tmp/testglom/testglom.glom");
		final String tempFilename = "testglom";
		File tempFolder = Files.createTempDir();
		final File tempDir = new File(tempFolder, tempFilename);

		final String tempDirPath = tempDir.getPath();
		final String tempFilePath = tempDirPath + File.separator + tempFilename;
		final File file = new File(tempFilePath);

		// Make sure that the file does not exist yet:
		{
			tempDir.delete();
		}

		// Save the example as a real file:
		document.setFileURI(file.getPath());

		document.setHostingMode(hostingMode);
		document.setIsExampleFile(false);
		final boolean saved = document.save();
		if (!saved) {
			// TODO: Warn
			return false; // TODO: Delete the directory.
		}

		// We must specify a default username and password:
		final String user = "glom_default_developer_user";
		final String password = "glom_default_developer_password";

		// Create the self-hosting files:
		if (!initialize(user, password)) {
			// TODO: Warn.
			// TODO: Delete directory.
		}

		// Check that it really created some files:
		if (!tempDir.exists()) {
			// TODO: Warn
			// TODO: Delete directory.
		}

		return selfHost(user, password);
	}

	/**
	 * @param document
	 * @param user
	 * @param password
	 * @return
	 */
	private boolean selfHost(final String user, final String password) {
		// TODO: m_network_shared = network_shared;

		if (getSelfHostingActive()) {
			// TODO: std::cerr << G_STRFUNC << ": Already started." << std::endl;
			return false; // STARTUPERROR_NONE; //Just do it once.
		}

		final String dbDirData = getSelfHostingDataPath(false);
		if (StringUtils.isEmpty(dbDirData) || !fileExists(dbDirData)) {
			/*
			 * final String dbDirBackup = dbDir + File.separator + FILENAME_BACKUP;
			 * 
			 * if(fileExists(dbDirBackup)) { //TODO: std::cerr << G_STRFUNC <<
			 * ": There is no data, but there is backup data." << std::endl; //Let the caller convert the backup to real
			 * data and then try again: return false; // STARTUPERROR_FAILED_NO_DATA_HAS_BACKUP_DATA; } else {
			 */
			// TODO: std::cerr << "ConnectionPool::create_self_hosting(): The data sub-directory could not be found." <<
			// dbdir_data_uri << std::endl;
			return false; // STARTUPERROR_FAILED_NO_DATA;
			// }
		}

		final int availablePort = discoverFirstFreePort(PORT_POSTGRESQL_SELF_HOSTED_START,
				PORT_POSTGRESQL_SELF_HOSTED_END);
		// std::cout << "debug: " << G_STRFUNC << ":() : debug: Available port for self-hosting: " << available_port <<
		// std::endl;
		if (availablePort == 0) {
			// TODO: Use a return enum or exception so we can tell the user about this:
			// TODO: std::cerr << G_STRFUNC << ": No port was available between " << PORT_POSTGRESQL_SELF_HOSTED_START
			// << " and " << PORT_POSTGRESQL_SELF_HOSTED_END << std::endl;
			return false; // STARTUPERROR_FAILED_UNKNOWN_REASON;
		}

		final NumberFormat format = NumberFormat.getInstance(Locale.US);
		format.setGroupingUsed(false); // TODO: Does this change it system-wide?
		final String portAsText = format.format(availablePort);

		// -D specifies the data directory.
		// -c config_file= specifies the configuration file
		// -k specifies a directory to use for the socket. This must be writable by us.
		// Make sure to use double quotes for the executable path, because the
		// CreateProcess() API used on Windows does not support single quotes.
		final String dbDir = getSelfHostingPath("", false);
		final String dbDirConfig = getSelfHostingPath("config", false);
		final String dbDirHba = dbDirConfig + File.separator + "pg_hba.conf";
		final String dbDirIdent = dbDirConfig + File.separator + "pg_ident.conf";
		final String dbDirPid = getSelfHostingPath("pid", false);

		// Note that postgres returns this error if we split the arguments more,
		// for instance splitting -D and dbDirData into separate strings:
		// too many command-line arguments (first is "(null)")
		// Note: If we use "-D " instead of "-D" then the initdb seems to make the space part of the filepath,
		// though that does not happen with the normal command line.
		// However, we must have a space after -k.
		// Also, the c hba_file=path argument must be split after -c, or postgres will get a " hba_file" configuration
		// parameter instead of "hba_file".
		final ProcessBuilder commandPostgresStart = new ProcessBuilder(getPathToPostgresExecutable("postgres"), "-D"
				+ shellQuote(dbDirData), "-p", portAsText, "-i", // Equivalent to -h "*", which in turn is equivalent
																	// to
				// listen_addresses in postgresql.conf. Listen to all IP addresses,
				// so any client can connect (with a username+password)
				"-c", "hba_file=" + shellQuote(dbDirHba), "-c", "ident_file=" + shellQuote(dbDirIdent), "-k"
						+ shellQuote(dbDir), "--external_pid_file=" + shellQuote(dbDirPid));
		// std::cout << G_STRFUNC << ": debug: " << command_postgres_start << std::endl;

		// Make sure to use double quotes for the executable path, because the
		// CreateProcess() API used on Windows does not support single quotes.
		//
		// Note that postgres returns this error if we split the arguments more,
		// for instance splitting -D and dbDirData into separate strings:
		// too many command-line arguments (first is "(null)")
		// Note: If we use "-D " instead of "-D" then the initdb seems to make the space part of the filepath,
		// though that does not happen with the normal command line.
		final ProcessBuilder commandCheckPostgresHasStarted = new ProcessBuilder(getPathToPostgresExecutable("pg_ctl"),
				"status", "-D" + shellQuote(dbDirData));

		// For postgres 8.1, this is "postmaster is running".
		// For postgres 8.2, this is "server is running".
		// This is a big hack that we should avoid. murrayc.
		//
		// pg_ctl actually seems to return a 0 result code for "is running" and a 1 for not running, at least with
		// Postgres 8.2,
		// so maybe we can avoid this in future.
		// Please do test it with your postgres version, using "echo $?" to see the result code of the last command.
		final String secondCommandSuccessText = "is running"; // TODO: This is not a stable API. Also, watch out for
																// localisation.

		// The first command does not return, but the second command can check whether it succeeded:
		// TODO: Progress
		final boolean result = executeCommandLineAndWaitUntilSecondCommandReturnsSuccess(commandPostgresStart,
				commandCheckPostgresHasStarted, secondCommandSuccessText);
		if (!result) {
			// TODO: std::cerr << "Error while attempting to self-host a database." << std::endl;
			return false; // STARTUPERROR_FAILED_UNKNOWN_REASON;
		}

		// Remember the port for later:
		document.setConnectionPort(availablePort);

		return true; // STARTUPERROR_NONE;
	}

	/**
	 * @param dbDirData
	 * @return
	 */
	private String shellQuote(String str) {
		// TODO: If we add the quotes then they seem to be used as part of the path, though that is not a problem with
		// the normal command line.
		return str;

		// TODO: Escape.
		// return "'" + str + "'";
	}

	private String getSelfHostingPath(final String subpath, boolean create) {
		final String dbDir = document.getSelfHostedDirectoryPath();
		if (StringUtils.isEmpty(subpath)) {
			return dbDir;
		}

		final String dbDirData = dbDir + File.separator + subpath;
		final File file = new File(dbDirData);

		// Return the path regardless of whether it exists:
		if (!create) {
			return dbDirData;
		}

		if (!file.exists()) {
			try {
				Files.createParentDirs(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}

			if (!file.mkdir()) {
				return "";
			}
		}

		return dbDirData;
	}

	private String getSelfHostingDataPath(boolean create) {
		return getSelfHostingPath(FILENAME_DATA, create);
	}

	private boolean executeCommandLineAndWait(ProcessBuilder command) {

		command.redirectErrorStream(true);

		// Run the first command, and wait for it to return:
		Process process = null;
		try {
			process = command.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		final InputStream stderr = process.getInputStream();
		final InputStreamReader isr = new InputStreamReader(stderr);
		final BufferedReader br = new BufferedReader(isr);
		String output = "";
		String line;
		try {
			while ((line = br.readLine()) != null) {
				output += line + "\n";
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		int result = 0;
		try {
			result = process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		if (result != 0) {
			System.out.println("Command failed: " + command.toString());
			System.out.print("Output: " + output);
			return false;
		}

		return true;
	}

	private boolean executeCommandLineAndWaitUntilSecondCommandReturnsSuccess(ProcessBuilder command,
			ProcessBuilder commandSecond, String secondCommandSuccessText) {
		command.redirectErrorStream(true);

		// Run the first command, and do not wait for it to return:
		Process process = null;
		try {
			process = command.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		final InputStream stderr = process.getInputStream();
		final InputStreamReader isr = new InputStreamReader(stderr);
		final BufferedReader br = new BufferedReader(isr);

		/*
		 * We do not wait, because this (postgres, for instance), does not return: final int result = process.waitFor();
		 * if (result != 0) { // TODO: Warn. return false; }
		 */

		// Now run the second command, usually to verify that the first command has really done its work:
		final boolean result = executeCommandLineAndWait(commandSecond);

		// Try to get the output:
		if (!result) {
			String output = "";
			String line;
			try {
				while ((line = br.readLine()) != null) {
					output += line + "\n";
					System.out.println(line);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}

			System.out.print("Output of first command: " + output);
		}

		return result;
	}

	/**
	 * @param string
	 * @return
	 */
	private static String getPathToPostgresExecutable(String string) {
		// TODO: Test other locations.
		return "/usr/bin/" + string;
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	private static int discoverFirstFreePort(int start, int end) {
		for (int port = start; port <= end; ++port) {
			try {
				ServerSocket socket = new ServerSocket(port);

				// If the instantiation succeeded then the port was free:
				return socket.getLocalPort(); // This must equal port.
			} catch (IOException ex) {
				continue; // try next port
			}
		}

		return 0;
	}

	/**
	 * @param dbDir
	 * @return
	 */
	private static boolean fileExists(String filePath) {
		final File file = new File(filePath);
		return file.exists();
	}

	private void setSelfHostingActive(boolean selfHostingActive) {
		this.selfHostingActive = selfHostingActive;
	}

	/**
	 * @return
	 */
	private boolean getSelfHostingActive() {
		return selfHostingActive;
	}

	/**
	 * @param cpds
	 * @return
	 */
	private boolean initialize(final String initialUsername, final String initialPassword) {
		if (!initializeConfFiles()) {
			// TODO: Warn
			return false;
		}

		// initdb creates a new postgres database cluster:

		// Get file:// URI for the tmp/ directory:
		File filePwFile = null;
		try {
			filePwFile = File.createTempFile("glom_initdb_pwfile", "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String tempPwFile = filePwFile.getPath();

		final boolean pwfileCreationSucceeded = createTextFile(tempPwFile, initialPassword);
		if (!pwfileCreationSucceeded) {
			// TODO: Warn.
			return false;
		}

		// Make sure to use double quotes for the executable path, because the
		// CreateProcess() API used on Windows does not support single quotes.
		final String dbDirData = getSelfHostingDataPath(false /* create */);

		// Note that initdb returns this error if we split the arguments more,
		// for instance splitting -D and dbDirData into separate strings:
		// too many command-line arguments (first is "(null)")
		// TODO: If we quote tempPwFile then initdb says that it cannot find it.
		// Note: If we use "-D " instead of "-D" then the initdb seems to make the space part of the filepath,
		// though that does not happen with the normal command line.
		final ProcessBuilder commandInitdb = new ProcessBuilder(getPathToPostgresExecutable("initdb"), "-D"
				+ shellQuote(dbDirData), "-U", initialUsername, "--pwfile=" + tempPwFile);

		// Note that --pwfile takes the password from the first line of a file. It's an alternative to supplying it when
		// prompted on stdin.
		final boolean result = executeCommandLineAndWait(commandInitdb);
		if (!result) {
			// TODO: std::cerr << "Error while attempting to create self-hosting database." << std::endl;
			return false;
		}

		// Of course, we don't want this to stay around. It would be a security risk.
		File fileTempPwFile = new File(tempPwFile);
		if (!fileTempPwFile.delete()) {
			// TODO: Warn.
		}

		// Save the username and password for later;
		this.username = initialUsername;
		this.password = initialPassword;

		return result; // ? INITERROR_NONE : INITERROR_COULD_NOT_START_SERVER;

	}

	private boolean initializeConfFiles() {
		final String dataDirPath = document.getSelfHostedDirectoryPath();

		final String dbDirConfig = dataDirPath + File.separator + "config";
		// String defaultConfContents = "";

		// Choose the configuration contents based on the postgresql version
		// and whether we want to be network-shared:
		// final float postgresqlVersion = 9.0f; //TODO: get_postgresql_utils_version_as_number(slot_progress);
		// final boolean networkShared = true;
		// std::cout << "DEBUG: postgresql_version=" << postgresql_version << std::endl;

		// TODO: Support the other configurations, as in libglom.
		final String defaultConfContents = DEFAULT_CONFIG_PG_HBA_LOCAL_8p4;

		// std::cout << "DEBUG: default_conf_contents=" << default_conf_contents << std::endl;

		final boolean hbaConfCreationSucceeded = createTextFile(dbDirConfig + File.separator + "pg_hba.conf",
				defaultConfContents);
		if (!hbaConfCreationSucceeded) {
			// TODO: Warn
			return false;
		}

		final boolean identConfCreationSucceeded = createTextFile(dbDirConfig + File.separator + "pg_ident.conf",
				DEFAULT_CONFIG_PG_IDENT);
		if (!identConfCreationSucceeded) {
			// TODO: Warn
			return false;
		}

		return true;
	}

	/**
	 * @param path
	 * @param contents
	 * @return
	 */
	private static boolean createTextFile(final String path, final String contents) {
		final File file = new File(path);
		final File parent = file.getParentFile();
		if (parent == null) {
			// TODO: Warn.
			return false;
		}

		parent.mkdirs();
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			output.write(contents.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @param document
	 * @return
	 */
	private boolean recreateDatabaseFromDocument() {
		// Check whether the database exists already.
		final String dbName = document.getConnectionDatabase();
		if (StringUtils.isEmpty(dbName)) {
			return false;
		}

		document.setConnectionDatabase(dbName);
		Connection connection = createConnection();
		if (connection != null) {
			// Connection to the database succeeded, so the database
			// exists already.
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		// Create the database:
		progress();
		document.setConnectionDatabase("");

		connection = createConnection();
		if (connection == null) {
			// TODO: Warn.
			return false;
		}

		final boolean dbCreated = createDatabase(connection, dbName);

		if (!dbCreated) {
			return false;
		}

		progress();

		// Check that we can connect:
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connection = null;

		document.setConnectionDatabase(dbName);
		connection = createConnection();
		if (connection == null) {
			// TODO: Warn
			return false;
		}

		progress();

		// Create each table:
		final List<String> tables = document.getTableNames();
		for (String tableName : tables) {

			// Create SQL to describe all fields in this table:
			final List<Field> fields = document.getTableFields(tableName);

			progress();
			final boolean tableCreationSucceeded = createTable(connection, document, tableName, fields);
			progress();
			if (!tableCreationSucceeded) {
				// TODO: std::cerr << G_STRFUNC << ": CREATE TABLE failed with the newly-created database." <<
				// std::endl;
				return false;
			}
		}

		// Note that create_database() has already called add_standard_tables() and add_standard_groups(document).

		// Add groups from the document:
		progress();
		if (!addGroupsFromDocument(document)) {
			// TODO: std::cerr << G_STRFUNC << ": add_groups_from_document() failed." << std::endl;
			return false;
		}

		// Set table privileges, using the groups we just added:
		progress();
		if (!setTablePrivilegesGroupsFromDocument(document)) {
			// TODO: std::cerr << G_STRFUNC << ": set_table_privileges_groups_from_document() failed." << std::endl;
			return false;
		}

		for (String tableName : tables) {
			// Add any example data to the table:
			progress();

			// try
			// {
			progress();
			final boolean tableInsertSucceeded = insertExampleData(connection, document, tableName);

			if (!tableInsertSucceeded) {
				// TODO: std::cerr << G_STRFUNC << ": INSERT of example data failed with the newly-created database." <<
				// std::endl;
				return false;
			}
			// }
			// catch(final std::exception& ex)
			// {
			// std::cerr << G_STRFUNC << ": exception: " << ex.what() << std::endl;
			// HandleError(ex);
			// }

		} // for(tables)

		return true; // All tables created successfully.
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	private Connection createConnection() {
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.username);
		connectionProps.put("password", this.password);

		String jdbcURL = "jdbc:postgresql://" + document.getConnectionServer() + ":" + document.getConnectionPort();
		String db = document.getConnectionDatabase();
		if (StringUtils.isEmpty(db)) {
			// Use the default PostgreSQL database, because ComboPooledDataSource.connect() fails otherwise.
			db = "template1";
		}
		jdbcURL += "/" + db; // TODO: Quote the database name?

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(jdbcURL + "/", connectionProps);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}

		return conn;
	}

	/**
	 *
	 */
	private void progress() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param document
	 * @param tableName
	 * @return
	 */
	private boolean insertExampleData(final Connection connection, final Document document, final String tableName) {

		final Factory factory = new Factory(connection, SQLDialect.POSTGRES);
		final Table<Record> table = Factory.tableByName(tableName);

		final List<Map<String, DataItem>> exampleRows = document.getExampleRows(tableName);
		for (final Map<String, DataItem> row : exampleRows) {
			InsertSetStep<Record> insertStep = factory.insertInto(table);

			for (final Entry<String, DataItem> entry : row.entrySet()) {
				final String fieldName = entry.getKey();
				final DataItem value = entry.getValue();
				if (value == null) {
					continue;
				}

				final Field field = document.getField(tableName, fieldName);
				if (field == null) {
					continue;
				}

				final org.jooq.Field<Object> jooqField = Factory.fieldByName(field.getName());
				if (jooqField == null) {
					continue;
				}

				final Object fieldValue = value.getValue(field.getGlomType());
				insertStep = insertStep.set(jooqField, fieldValue);
			}

			if (!(insertStep instanceof InsertResultStep<?>)) {
				continue;
			}

			// We suppress the warning because we _do_ check the cast above.
			@SuppressWarnings("unchecked")
			final InsertResultStep<Record> insertResultStep = (InsertResultStep<Record>) insertStep;

			try {
				final Record record = insertResultStep.fetchOne();
			} catch (DataAccessException e) {
				// e.printStackTrace();
				return false;
			}
			// TODO: Check that it worked.
		}

		return true;
	}

	/**
	 * @param document2
	 * @return
	 */
	private boolean setTablePrivilegesGroupsFromDocument(Document document2) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @param document2
	 * @return
	 */
	private boolean addGroupsFromDocument(Document document2) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @param document
	 * @param tableName
	 * @param fields
	 * @return
	 */
	private boolean createTable(final Connection connection, final Document document, final String tableName,
			final List<Field> fields) {
		boolean tableCreationSucceeded = false;

		/*
		 * TODO: //Create the standard field too: //(We don't actually use this yet) if(std::find_if(fields.begin(),
		 * fields.end(), predicate_FieldHasName<Field>(GLOM_STANDARD_FIELD_LOCK)) == fields.end()) { sharedptr<Field>
		 * field = sharedptr<Field>::create(); field->set_name(GLOM_STANDARD_FIELD_LOCK);
		 * field->set_glom_type(Field::TYPE_TEXT); fields.push_back(field); }
		 */

		// Create SQL to describe all fields in this table:
		String sqlFields = "";
		for (Field field : fields) {
			// Create SQL to describe this field:
			String sqlFieldDescription = escapeSqlId(field.getName()) + " " + field.getSqlType();

			if (field.getPrimaryKey())
				sqlFieldDescription += " NOT NULL  PRIMARY KEY";

			// Append it:
			if (!StringUtils.isEmpty(sqlFields)) {
				sqlFields += ", ";
			}

			sqlFields += sqlFieldDescription;
		}

		if (StringUtils.isEmpty(sqlFields)) {
			// TODO: std::cerr << G_STRFUNC << ": sql_fields is empty." << std::endl;
		}

		// Actually create the table
		final String query = "CREATE TABLE " + escapeSqlId(tableName) + " (" + sqlFields + ");";
		Factory factory = new Factory(connection, SQLDialect.POSTGRES);
		final int result = factory.execute(query);
		tableCreationSucceeded = true;
		if (!tableCreationSucceeded) {
			// TODO: Warn: std::cerr << G_STRFUNC << ": CREATE TABLE failed." << std::endl;
		}

		return tableCreationSucceeded;
	}

	/**
	 * @param name
	 * @return
	 */
	private String escapeSqlId(String name) {
		// TODO:
		return "\"" + name + "\"";
	}

	/**
	 * @return
	 */
	private static boolean createDatabase(final Connection connection, final String databaseName) {

		final String query = "CREATE DATABASE \"" + databaseName + "\""; // TODO: Escaping.
		Factory factory = new Factory(connection, SQLDialect.POSTGRES);

		final int result = factory.execute(query);

		return true;
	}

	/**
	 *
	 */
	public void cleanup() {

		// Stop the server:
		if ((document != null) && (document.getConnectionPort() != 0)) {
			final String dbDirData = getSelfHostingDataPath(false);

			// -D specifies the data directory.
			// -c config_file= specifies the configuration file
			// -k specifies a directory to use for the socket. This must be writable by us.
			// We use "-m fast" instead of the default "-m smart" because that waits for clients to disconnect (and
			// sometimes never succeeds).
			// TODO: Warn about connected clients on other computers? Warn those other users?
			// Make sure to use double quotes for the executable path, because the
			// CreateProcess() API used on Windows does not support single quotes.
			final ProcessBuilder commandPostgresStop = new ProcessBuilder(getPathToPostgresExecutable("pg_ctl"), "-D"
					+ shellQuote(dbDirData), "stop", "-m", "fast");
			final boolean result = executeCommandLineAndWait(commandPostgresStop);
			if (!result) {
				// TODO: Warn
				// return;
			}

			document.setConnectionPort(0);
		}

		// Delete the files:
		final String selfhostingPath = getSelfHostingPath("", false);
		final File fileSelfHosting = new File(selfhostingPath);
		fileSelfHosting.delete();

		final String docPath = document.getFileURI();
		final File fileDoc = new File(docPath);
		fileDoc.delete();
	}
}