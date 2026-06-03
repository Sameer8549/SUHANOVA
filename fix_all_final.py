import os
import re

def fix_nav():
    nav_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\Navigation.kt"
    with open(nav_path, 'r', encoding='utf-8') as f:
        nav = f.read()

    if "import androidx.navigation3.runtime.NavEntry" not in nav:
        nav = nav.replace("import androidx.navigation3.runtime.rememberNavBackStack", "import androidx.navigation3.runtime.rememberNavBackStack\nimport androidx.navigation3.runtime.NavEntry")

    nav = nav.replace("is Home     -> HomeScreen(onNavigate = ::navigateTo)", "is Home     -> NavEntry(Home) { HomeScreen(onNavigate = ::navigateTo) }")
    nav = nav.replace("is Study    -> StudyScreen(onNavigate = ::navigateTo)", "is Study    -> NavEntry(Study) { StudyScreen(onNavigate = ::navigateTo) }")
    nav = nav.replace("is Quiz     -> QuizScreen(onNavigate  = ::navigateTo)", "is Quiz     -> NavEntry(Quiz) { QuizScreen(onNavigate  = ::navigateTo) }")
    nav = nav.replace("is Progress -> ProgressScreen()", "is Progress -> NavEntry(Progress) { ProgressScreen() }")
    nav = nav.replace("is Rewards  -> RewardsScreen()", "is Rewards  -> NavEntry(Rewards) { RewardsScreen() }")
    nav = nav.replace("is Library  -> LibraryScreen()", "is Library  -> NavEntry(Library) { LibraryScreen() }")
    nav = nav.replace("is Roadmap  -> RoadmapScreen()", "is Roadmap  -> NavEntry(Roadmap) { RoadmapScreen() }")
    nav = nav.replace("is NovaChat -> NovaChatScreen()", "is NovaChat -> NavEntry(NovaChat) { NovaChatScreen() }")
    nav = nav.replace("else        -> HomeScreen(onNavigate = ::navigateTo)", "else        -> NavEntry(Home) { HomeScreen(onNavigate = ::navigateTo) }")

    with open(nav_path, 'w', encoding='utf-8') as f:
        f.write(nav)


def fix_file_general(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        c = f.read()

    # Generic replaces for positional params that I commonly missed
    c = c.replace("Row(Arrangement.spacedBy(8.dp), Alignment.CenterVertically)", "Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically)")
    c = c.replace("Row(Arrangement.spacedBy(10.dp), Alignment.CenterVertically)", "Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically)")
    c = c.replace("Row(Arrangement.SpaceBetween, Alignment.CenterVertically)", "Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)")
    
    # LazyRow
    c = re.sub(r'LazyRow\(\s*(Modifier\.[^,]+),\s*(Arrangement\.[^)]+\))', r'LazyRow(\1, horizontalArrangement = \2', c)
    c = re.sub(r'LazyRow\(\s*(Modifier\.[^,]+),\s*(Arrangement\.Space[^,]+)', r'LazyRow(\1, horizontalArrangement = \2', c)
    
    # LazyColumn
    c = re.sub(r'LazyColumn\(\s*(Modifier\.[^,]+),\s*(Arrangement\.[^)]+\))', r'LazyColumn(\1, verticalArrangement = \2', c)
    c = re.sub(r'LazyColumn\(\s*(Modifier\.[^,]+),\s*(Arrangement\.Space[^,]+)', r'LazyColumn(\1, verticalArrangement = \2', c)

    # Column
    c = re.sub(r'Column\(\s*(Modifier\.[^,]+),\s*(Alignment\.[^,]+),\s*(Arrangement\.[^)]+\))', r'Column(\1, horizontalAlignment = \2, verticalArrangement = \3', c)
    c = re.sub(r'Column\(\s*(Modifier\.[^,]+),\s*(Alignment\.[^,]+)', r'Column(\1, horizontalAlignment = \2', c)

    # Row
    c = re.sub(r'Row\(\s*(Modifier\.[^,]+),\s*(Arrangement\.[^)]+\)),\s*(Alignment\.[^,]+)', r'Row(\1, horizontalArrangement = \2, verticalAlignment = \3', c)
    c = re.sub(r'Row\(\s*(Modifier\.[^,]+),\s*(Arrangement\.Space[^,]+),\s*(Alignment\.[^,]+)', r'Row(\1, horizontalArrangement = \2, verticalAlignment = \3', c)
    c = re.sub(r'Row\(\s*(Modifier\.[^,]+),\s*(Arrangement\.[^)]+\))', r'Row(\1, horizontalArrangement = \2', c)
    c = re.sub(r'Row\(\s*(Modifier\.[^,]+),\s*(Arrangement\.Space[^,]+)', r'Row(\1, horizontalArrangement = \2', c)

    # Clean up double assignments that regex might create
    c = c.replace("horizontalArrangement = horizontalArrangement = ", "horizontalArrangement = ")
    c = c.replace("verticalArrangement = verticalArrangement = ", "verticalArrangement = ")
    c = c.replace("horizontalAlignment = horizontalAlignment = ", "horizontalAlignment = ")
    c = c.replace("verticalAlignment = verticalAlignment = ", "verticalAlignment = ")

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(c)

def main():
    fix_nav()
    
    base_dir = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\ui"
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith('.kt'):
                fix_file_general(os.path.join(root, file))

if __name__ == "__main__":
    main()
