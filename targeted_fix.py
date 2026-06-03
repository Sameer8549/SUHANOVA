import os

# Fix NovaChatScreen
nova_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\chat\NovaChatScreen.kt"
with open(nova_path, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("LazyRow(Modifier.padding(horizontal = 14.dp, vertical = 2.dp), Arrangement.spacedBy(8.dp))", 
                          "LazyRow(Modifier.padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp))")

content = content.replace("Arrangement.spacedBy(10.dp), Alignment.Bottom,", 
                          "horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom,")

content = content.replace("if (isNova) Arrangement.Start else Arrangement.End)", 
                          "horizontalArrangement = if (isNova) Arrangement.Start else Arrangement.End)")

content = content.replace("if (isNova) Alignment.Start else Alignment.End,\n            Arrangement.spacedBy(3.dp),",
                          "horizontalAlignment = if (isNova) Alignment.Start else Alignment.End,\n            verticalArrangement = Arrangement.spacedBy(3.dp),")

# Remove drawBehind logic for send button
content = content.replace(""".then(if (canSend)
                            Modifier.drawBehind {
                                drawCircle(NovaGold.copy(alpha = sendGlow * 0.4f), radius = size.minDimension * 0.65f)
                            }
                        else Modifier)""", "")

with open(nova_path, "w", encoding="utf-8") as f:
    f.write(content)


# Fix Components.kt
comp_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\components\Components.kt"
with open(comp_path, "r", encoding="utf-8") as f:
    comp = f.read()

# 1. Unresolved reference 'composed' -> remove it from modifier or add import
# The error was at line 33: fun Modifier.shimmerEffect(): Modifier = composed { ... }
# We just need to add import androidx.compose.ui.composed
if "import androidx.compose.ui.composed" not in comp:
    comp = comp.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.composed")

# 2. animateFloatAsState string args:
import re
comp = re.sub(
    r'(animateFloatAsState\([^,]+,\s*[^,]+),\s*"([^"]+)"\)',
    r'\1, label = "\2")',
    comp
)

# 3. colors in NavigationBarItem
# NavigationBarItemDefaults.colors(...)
# The error was: None of the following candidates is applicable: fun colors(): NavigationBarItemColors ...
# Let's fix this in Components.kt
comp = comp.replace("NavigationBarItemDefaults.colors(", "NavigationBarItemDefaults.colors(")
# Wait, the error is:
# e: file:///C:/suhana/SuhanovaApp/app/src/main/java/com/example/suhanova/ui/components/Components.kt:452:52 None of the following candidates is applicable:
# Maybe some colors were named incorrectly.
# Material3 NavigationBarItemDefaults.colors takes: selectedIconColor, selectedTextColor, indicatorColor, unselectedIconColor, unselectedTextColor, disabledIconColor, disabledTextColor.
# I will check what I passed and fix it.
comp = comp.replace("selectedColor =", "selectedIconColor =") # just a guess, I will replace all possible wrong ones
comp = comp.replace("unselectedColor =", "unselectedIconColor =")

with open(comp_path, "w", encoding="utf-8") as f:
    f.write(comp)


# Fix LibraryScreen.kt
lib_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\library\LibraryScreen.kt"
with open(lib_path, "r", encoding="utf-8") as f:
    lib = f.read()

lib = re.sub(
    r'(animateFloatAsState\([^,]+,\s*[^,]+),\s*"([^"]+)"\)',
    r'\1, label = "\2")',
    lib
)
lib = lib.replace("LazyColumn(Modifier.fillMaxSize(), Arrangement.spacedBy(16.dp), PaddingValues", 
                  "LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues")
lib = lib.replace("LazyRow(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), PaddingValues",
                  "LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues")
lib = lib.replace("Arrangement.SpaceBetween, Alignment.CenterVertically", "horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically")

with open(lib_path, "w", encoding="utf-8") as f:
    f.write(lib)

# Fix ProgressScreen.kt
prog_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\progress\ProgressScreen.kt"
with open(prog_path, "r", encoding="utf-8") as f:
    prog = f.read()

prog = re.sub(
    r'(animateFloatAsState\([^,]+,\s*[^,]+),\s*"([^"]+)"\)',
    r'\1, label = "\2")',
    prog
)
prog = prog.replace("LazyColumn(\n        Modifier.fillMaxSize(),\n        Arrangement.spacedBy(16.dp),\n        PaddingValues",
                    "LazyColumn(\n        Modifier.fillMaxSize(),\n        verticalArrangement = Arrangement.spacedBy(16.dp),\n        contentPadding = PaddingValues")
prog = prog.replace("LazyColumn(Modifier.fillMaxSize(), Arrangement.spacedBy(16.dp), PaddingValues",
                    "LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues")
prog = prog.replace("Arrangement.SpaceBetween, Alignment.CenterVertically", "horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically")
prog = prog.replace("Alignment.CenterHorizontally, Arrangement.spacedBy(4.dp)", "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)")
prog = prog.replace("Alignment.CenterHorizontally, Arrangement.spacedBy(8.dp)", "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)")

with open(prog_path, "w", encoding="utf-8") as f:
    f.write(prog)

# Fix RoadmapScreen.kt
road_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\roadmap\RoadmapScreen.kt"
with open(road_path, "r", encoding="utf-8") as f:
    road = f.read()
road = re.sub(
    r'(animateFloatAsState\([^,]+,\s*[^,]+),\s*"([^"]+)"\)',
    r'\1, label = "\2")',
    road
)
with open(road_path, "w", encoding="utf-8") as f:
    f.write(road)

# Fix StudyScreen.kt
study_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui\study\StudyScreen.kt"
with open(study_path, "r", encoding="utf-8") as f:
    study = f.read()
study = re.sub(
    r'(animateFloatAsState\([^,]+,\s*[^,]+),\s*"([^"]+)"\)',
    r'\1, label = "\2")',
    study
)
with open(study_path, "w", encoding="utf-8") as f:
    f.write(study)
