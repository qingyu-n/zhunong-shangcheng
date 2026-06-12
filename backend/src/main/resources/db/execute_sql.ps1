$sqlPath = "d:\Trea ide\pj\助农商城\backend\src\main\resources\db\add_is_delete_to_all_tables.sql"
Get-Content $sqlPath | & "mysql" -u root -p123456 zhunong-shangcheng
