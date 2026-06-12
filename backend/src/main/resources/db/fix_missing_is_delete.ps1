$sqlPath = "d:\Trea ide\pj\助农商城\backend\src\main\resources\db\fix_missing_is_delete.sql"
Get-Content $sqlPath | mysql -u root -p123456 zhunong-shangcheng
