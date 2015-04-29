# mdb-oracle
[![Build Status](https://travis-ci.org/codemumbler/mdb-oracle.svg?branch=master)](https://travis-ci.org/codemumbler/mdb-oracle)

Converts MS Access files into Oracle SQL scripts.

Usage example:

```
  Database database = null;
		try {
			MDBReader reader = new MDBReader(accessFile);
			database = reader.loadDatabase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			OracleScriptWriter writer = new OracleScriptWriter(database);
			PrintWriter printWriter = new PrintWriter(outputFile);
			printWriter.println(writer.writeScript());
			printWriter.flush();
			printWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
	}
```
