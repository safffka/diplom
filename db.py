import sqlite3
from sqlite3 import Error

def create_connection(db_file):
    """ create a database connection to the SQLite database specified by db_file """
    conn = None
    try:
        conn = sqlite3.connect(db_file)
        print(f"SQLite version {sqlite3.version}. Connection to {db_file} established.")
    except Error as e:
        print(e)
    return conn

def create_table(conn, create_table_sql):
    """ create a table from the create_table_sql statement """
    try:
        c = conn.cursor()
        c.execute(create_table_sql)
    except Error as e:
        print(e)
def insert_user_data(conn, user_data):
    """
    Insert new user data into the users table
    :param conn: Connection object to the database
    :param user_data: A tuple containing (login, phone, video, plates)
    """
    sql = ''' INSERT INTO users(login, phone, video, plates)
              VALUES(?,?,?,?) '''
    cur = conn.cursor()
    cur.execute(sql, user_data)
    conn.commit()
    return cur.lastrowid
def main():
    database = "user_video.db"

    sql_create_user_table = """
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        login TEXT NOT NULL,
        phone TEXT,
        video TEXT,
        plates TEXT
    );
    """

    # Create a database connection
    conn = create_connection(database)

    # Create table
    if conn is not None:
        create_table(conn, sql_create_user_table)
        print("Table 'users' created successfully.")
    else:
        print("Error! Cannot create the database connection.")

    # Close the connection
    conn.close()

if __name__ == '__main__':
    main()
