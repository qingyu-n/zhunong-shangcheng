@echo off
chcp 65001 >nul
echo ============================================
echo 助农商城数据库迁移脚本
echo 数据库: zhunong-shangcheng
echo ============================================
echo.

set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASS=123456
set DB_NAME=zhunong-shangcheng

echo [1/3] 正在创建数据库和表结构...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% < 01_create_database.sql
if %errorlevel% neq 0 (
    echo [错误] 创建数据库失败！
    pause
    exit /b 1
)
echo [完成] 数据库和表结构创建成功！
echo.

echo [2/3] 正在初始化数据...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% < 02_init_data.sql
if %errorlevel% neq 0 (
    echo [错误] 初始化数据失败！
    pause
    exit /b 1
)
echo [完成] 数据初始化成功！
echo.

echo ============================================
echo 数据库迁移完成！
echo 数据库名: %DB_NAME%
echo 管理员账号: admin / 123456
echo ============================================
pause
