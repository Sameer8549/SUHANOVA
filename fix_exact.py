import os
import re

def fix_all_exact():
    # 1. NovaChatScreen.kt
    f1 = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\chat\NovaChatScreen.kt"
    with open(f1, 'r', encoding='utf-8') as f:
        c1 = f.read()
    c1 = c1.replace("Row(Arrangement.spacedBy(5.dp), Alignment.CenterVertically)", "Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically)")
    with open(f1, 'w', encoding='utf-8') as f:
        f.write(c1)
        
    # 2. LibraryScreen.kt
    f2 = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\library\LibraryScreen.kt"
    with open(f2, 'r', encoding='utf-8') as f:
        c2 = f.read()
    c2 = c2.replace("verticalArrangement = Arrangement.spacedBy(12.dp), PaddingValues(bottom = 110.dp)", "contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)")
    c2 = c2.replace("Row(Arrangement.spacedBy(6.dp))", "Row(horizontalArrangement = Arrangement.spacedBy(6.dp))")
    with open(f2, 'w', encoding='utf-8') as f:
        f.write(c2)

    # 3. ProgressScreen.kt
    f3 = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\progress\ProgressScreen.kt"
    with open(f3, 'r', encoding='utf-8') as f:
        c3 = f.read()
    c3 = c3.replace("verticalArrangement = Arrangement.spacedBy(16.dp),\n        PaddingValues(", "verticalArrangement = Arrangement.spacedBy(16.dp),\n        contentPadding = PaddingValues(")
    c3 = c3.replace("verticalArrangement = Arrangement.spacedBy(16.dp), PaddingValues(", "verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(")
    with open(f3, 'w', encoding='utf-8') as f:
        f.write(c3)

    # 4. RoadmapScreen.kt
    f4 = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\roadmap\RoadmapScreen.kt"
    with open(f4, 'r', encoding='utf-8') as f:
        c4 = f.read()
    c4 = c4.replace("Row(Arrangement.spacedBy(6.dp))", "Row(horizontalArrangement = Arrangement.spacedBy(6.dp))")
    with open(f4, 'w', encoding='utf-8') as f:
        f.write(c4)
        
    # 5. StudyScreen.kt
    f5 = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\study\StudyScreen.kt"
    with open(f5, 'r', encoding='utf-8') as f:
        c5 = f.read()
    c5 = c5.replace("Row(Arrangement.spacedBy(8.dp))", "Row(horizontalArrangement = Arrangement.spacedBy(8.dp))")
    with open(f5, 'w', encoding='utf-8') as f:
        f.write(c5)

fix_all_exact()
