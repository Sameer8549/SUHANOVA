import os
import re

def fix_compose_params(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. animateFloatAsState without label parameter name
    content = re.sub(
        r'animateFloatAsState\(([^,]+),\s*([^,]+),\s*"([^"]+)"\)',
        r'animateFloatAsState(\1, \2, label = "\3")',
        content
    )

    # 2. LazyColumn positional arguments
    content = re.sub(
        r'LazyColumn\(\s*(Modifier[^,]*),\s*(Arrangement\.[^,]+),\s*(PaddingValues\([^)]+\)),?\s*\)',
        r'LazyColumn(\1, verticalArrangement = \2, contentPadding = \3)',
        content
    )

    # 3. LazyRow positional arguments
    content = re.sub(
        r'LazyRow\(\s*(Modifier[^,]*),\s*(Arrangement\.[^,]+),\s*(PaddingValues\([^)]+\)),?\s*\)',
        r'LazyRow(\1, horizontalArrangement = \2, contentPadding = \3)',
        content
    )
    
    # Missing Modifier name in LazyRow
    content = re.sub(
        r'LazyRow\(\s*horizontalArrangement\s*=\s*(Arrangement\.[^,]+),\s*(PaddingValues\([^)]+\)),?\s*\)',
        r'LazyRow(horizontalArrangement = \1, contentPadding = \2)',
        content
    )

    # 4. Column positional arguments (Alignment, Arrangement) -> (verticalArrangement, horizontalAlignment)
    content = re.sub(
        r'Column\(\s*(Modifier[^,]+),\s*(Alignment\.[a-zA-Z]+),\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*\)',
        r'Column(\1, horizontalAlignment = \2, verticalArrangement = \3)',
        content
    )
    
    # 5. Row positional arguments (Arrangement, Alignment) -> (horizontalArrangement, verticalAlignment)
    content = re.sub(
        r'Row\(\s*(Modifier[^,]+),\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*(Alignment\.[a-zA-Z]+),\s*\)',
        r'Row(\1, horizontalArrangement = \2, verticalAlignment = \3)',
        content
    )
    
    # Row without Modifier?
    # Row(Arrangement.spacedBy(10.dp), Alignment.CenterVertically) -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically)
    content = re.sub(
        r'Row\(\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*(Alignment\.[a-zA-Z]+),\s*\)',
        r'Row(horizontalArrangement = \1, verticalAlignment = \2)',
        content
    )

    # Column without Modifier?
    content = re.sub(
        r'Column\(\s*(Alignment\.[a-zA-Z]+),\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*\)',
        r'Column(horizontalAlignment = \1, verticalArrangement = \2)',
        content
    )

    # 6. OutlinedButtonDefaults fix for Material 3
    # QuizScreen.kt uses OutlinedButtonDefaults? Actually in Material3 it's ButtonDefaults.outlinedButtonColors()
    content = content.replace("OutlinedButtonDefaults.colors", "ButtonDefaults.outlinedButtonColors")

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

base_dir = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui"
fix_compose_params(os.path.join(base_dir, r"roadmap\RoadmapScreen.kt"))
fix_compose_params(os.path.join(base_dir, r"study\StudyScreen.kt"))
fix_compose_params(os.path.join(base_dir, r"quiz\QuizScreen.kt"))
