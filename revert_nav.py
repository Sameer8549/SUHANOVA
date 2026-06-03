import os

def revert_nav():
    nav_path = r"C:\suhana\SuhanovaApp\app\src\main\java\com\example\suhanova\Navigation.kt"
    with open(nav_path, 'r', encoding='utf-8') as f:
        nav = f.read()

    nav = nav.replace("import androidx.navigation3.runtime.rememberNavBackStack\nimport androidx.navigation3.runtime.NavEntry", "import androidx.navigation3.runtime.rememberNavBackStack")

    nav = nav.replace("is Home     -> NavEntry(Home) { HomeScreen(onNavigate = ::navigateTo) }", "is Home     -> HomeScreen(onNavigate = ::navigateTo)")
    nav = nav.replace("is Study    -> NavEntry(Study) { StudyScreen(onNavigate = ::navigateTo) }", "is Study    -> StudyScreen(onNavigate = ::navigateTo)")
    nav = nav.replace("is Quiz     -> NavEntry(Quiz) { QuizScreen(onNavigate  = ::navigateTo) }", "is Quiz     -> QuizScreen(onNavigate  = ::navigateTo)")
    nav = nav.replace("is Progress -> NavEntry(Progress) { ProgressScreen() }", "is Progress -> ProgressScreen()")
    nav = nav.replace("is Rewards  -> NavEntry(Rewards) { RewardsScreen() }", "is Rewards  -> RewardsScreen()")
    nav = nav.replace("is Library  -> NavEntry(Library) { LibraryScreen() }", "is Library  -> LibraryScreen()")
    nav = nav.replace("is Roadmap  -> NavEntry(Roadmap) { RoadmapScreen() }", "is Roadmap  -> RoadmapScreen()")
    nav = nav.replace("is NovaChat -> NavEntry(NovaChat) { NovaChatScreen() }", "is NovaChat -> NovaChatScreen()")
    nav = nav.replace("else        -> NavEntry(Home) { HomeScreen(onNavigate = ::navigateTo) }", "else        -> HomeScreen(onNavigate = ::navigateTo)")

    with open(nav_path, 'w', encoding='utf-8') as f:
        f.write(nav)

revert_nav()
