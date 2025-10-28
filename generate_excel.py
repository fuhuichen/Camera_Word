#!/usr/bin/env python3
"""
生成測試用的相機Excel文件
"""

import pandas as pd
from datetime import datetime, timedelta
import os

def generate_camera_excel():
    # 生成測試數據
    cameras_data = []
    
    # DK平台相機
    for i in range(1, 6):
        cameras_data.append({
            '相機序號': f'CAM_DK_{i:03d}',
            '出廠時間': '2024-10-27',
            '到期時間': '2025-10-27',
            '相機型號': f'Model-A{i}',
            '出貨批次': f'Batch-2024-Q4-{i:03d}'
        })
    
    # 兑心平台相機
    for i in range(1, 6):
        cameras_data.append({
            '相機序號': f'CAM_DUIXIN_{i:03d}',
            '出廠時間': '2024-10-27',
            '到期時間': '2025-10-27',
            '相機型號': f'Model-X{i}',
            '出貨批次': f'Batch-2024-Q4-{i+5:03d}'
        })
    
    # 創建DataFrame
    df = pd.DataFrame(cameras_data)
    
    # 確保目錄存在
    os.makedirs('sample_data', exist_ok=True)
    
    # 保存為Excel文件
    excel_file = 'sample_data/cameras_import_sample.xlsx'
    df.to_excel(excel_file, index=False, engine='openpyxl')
    
    print(f"Excel文件已生成: {excel_file}")
    print(f"包含 {len(cameras_data)} 個相機記錄")
    
    # 顯示前幾行數據
    print("\n前5行數據預覽:")
    print(df.head())
    
    return excel_file

if __name__ == "__main__":
    generate_camera_excel()
