import os

errors = """
e: file:///C:/suhana/SuhanovaApp/app/src/main/java/com/example/suhanova/ui/library/LibraryScreen.kt:359:21 Argument type mismatch: actual type is 'Arrangement.HorizontalOrVertical', but 'Modifier' was expected.
e: file:///C:/suhana/SuhanovaApp/app/src/main/java/com/example/suhanova/ui/library/LibraryScreen.kt:378:109 Argument type mismatch: actual type is 'String', but 'Float' was expected.
e: file:///C:/suhana/SuhanovaApp/app/src/main/java/com/example/suhanova/ui/progress/ProgressScreen.kt:61:9 Argument type mismatch: actual type is 'Arrangement.HorizontalOrVertical', but 'LazyListState' was expected.
e: file:///C:/suhana/SuhanovaApp/app/src/main/java/com/example/suhanova/ui/progress/ProgressScreen.kt:78:124 Argument type mismatch: actual type is 'String', but 'Float' was expected.
e: file:///C:/suhana/SuhanovaApp/app/src/main/java/com/example/suhanova/ui/roadmap/RoadmapScreen.kt:381:37 Argument type mismatch: actual type is 'Alignment.Horizontal', but 'Arrangement.Vertical' was expected.
e: file:///C:/suhana/SuhanovaApp/app/src/main/java/com/example/suhanova/ui/study/StudyScreen.kt:130:29 Argument type mismatch: actual type is 'Arrangement.HorizontalOrVertical', but 'Modifier' was expected.
"""

# Let's just fix the files manually since there aren't *that* many files, but wait, there are ~30 errors.
# Instead of manual, I'll write a Python script that replaces all instances of animateFloatAsState string args,
# and fixes the Row/Column positional arguments globally.

import re

def fix_all_compose_issues(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.kt'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()

                # Fix animateFloatAsState
                # Find all animateFloatAsState(..., "string") and change to label = "string"
                # It might have 2 or 3 parameters before the string.
                content = re.sub(
                    r'(animateFloatAsState\([^,]+,\s*[^,]+),\s*"([^"]+)"\)',
                    r'\1, label = "\2")',
                    content
                )
                
                # Fix LazyColumn without modifier
                # LazyColumn(Arrangement.spacedBy(14.dp), PaddingValues(...))
                content = re.sub(
                    r'LazyColumn\(\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*(PaddingValues\([^)]+\)),?\s*\)',
                    r'LazyColumn(verticalArrangement = \1, contentPadding = \2)',
                    content
                )
                
                # Fix LazyRow without modifier
                content = re.sub(
                    r'LazyRow\(\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*(PaddingValues\([^)]+\)),?\s*\)',
                    r'LazyRow(horizontalArrangement = \1, contentPadding = \2)',
                    content
                )

                # Fix Column without modifier
                content = re.sub(
                    r'Column\(\s*(Alignment\.[a-zA-Z]+),\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*\)',
                    r'Column(horizontalAlignment = \1, verticalArrangement = \2)',
                    content
                )

                # Fix Row without modifier
                content = re.sub(
                    r'Row\(\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*(Alignment\.[a-zA-Z]+),\s*\)',
                    r'Row(horizontalArrangement = \1, verticalAlignment = \2)',
                    content
                )
                
                # Row with Modifier but reversed positional args
                content = re.sub(
                    r'Row\(\s*(Modifier[^,]+),\s*(Alignment\.[a-zA-Z]+),\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*\)',
                    r'Row(\1, verticalAlignment = \2, horizontalArrangement = \3)',
                    content
                )
                
                # Column with Modifier but reversed positional args
                content = re.sub(
                    r'Column\(\s*(Modifier[^,]+),\s*(Arrangement\.[a-zA-Z]+(?:[^,]+)?),\s*(Alignment\.[a-zA-Z]+),\s*\)',
                    r'Column(\1, verticalArrangement = \2, horizontalAlignment = \3)',
                    content
                )

                # Fix Modifier weight/scale padding etc on Row/Column where it's parsed as multiple args by regex? No, the modifier regex might be failing if there are commas inside the modifier chain.
                # Actually, the errors are:
                # `Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically)`
                # Let's just do text replacements for the common patterns I used.
                
                content = content.replace(
                    "Arrangement.SpaceBetween, Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically"
                )
                content = content.replace(
                    "Arrangement.spacedBy(10.dp), Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically"
                )
                content = content.replace(
                    "Arrangement.spacedBy(12.dp), Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically"
                )
                content = content.replace(
                    "Arrangement.spacedBy(14.dp), Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically"
                )
                content = content.replace(
                    "Arrangement.spacedBy(8.dp), Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically"
                )
                content = content.replace(
                    "Arrangement.spacedBy(6.dp), Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically"
                )
                content = content.replace(
                    "Arrangement.spacedBy(4.dp), Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically"
                )
                content = content.replace(
                    "Arrangement.SpaceBetween, Alignment.Bottom",
                    "horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom"
                )
                content = content.replace(
                    "Arrangement.Center, Alignment.CenterVertically",
                    "horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically"
                )
                
                content = content.replace(
                    "Alignment.CenterHorizontally, Arrangement.spacedBy(4.dp)",
                    "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)"
                )
                content = content.replace(
                    "Alignment.CenterHorizontally, Arrangement.spacedBy(3.dp)",
                    "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)"
                )
                content = content.replace(
                    "Alignment.CenterHorizontally, Arrangement.spacedBy(2.dp)",
                    "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)"
                )
                content = content.replace(
                    "Alignment.CenterHorizontally, Arrangement.spacedBy(8.dp)",
                    "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)"
                )
                content = content.replace(
                    "Alignment.CenterHorizontally, Arrangement.spacedBy(12.dp)",
                    "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)"
                )
                content = content.replace(
                    "Alignment.CenterHorizontally, Arrangement.spacedBy(16.dp)",
                    "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)"
                )

                # Fix for LazyRow and LazyColumn
                content = content.replace(
                    "Arrangement.spacedBy(14.dp), PaddingValues",
                    "verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues"
                )
                content = content.replace(
                    "Arrangement.spacedBy(10.dp), PaddingValues",
                    "horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues"
                )
                content = content.replace(
                    "Arrangement.spacedBy(16.dp), PaddingValues",
                    "verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues"
                )
                
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)

fix_all_compose_issues(r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui")
