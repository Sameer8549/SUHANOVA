import os
import re

def fix_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Fix animateFloatAsState label
    content = re.sub(r'animateFloatAsState\(([^,]+), ([^,]+), "([^"]+)"\)',
                     r'animateFloatAsState(\1, \2, label = "\3")', content)

    # Fix LazyColumn
    content = re.sub(r'LazyColumn\(\s*([^,]+),\s*Arrangement\.spacedBy\(([^)]+)\),\s*PaddingValues\([^)]+\),\s*\)',
                     lambda m: m.group(0).replace('Arrangement.spacedBy', 'verticalArrangement = Arrangement.spacedBy').replace('PaddingValues', 'contentPadding = PaddingValues'), content)
    
    # Let's just do a generic regex for LazyColumn/LazyRow
    content = re.sub(r'Lazy(Column|Row)\(\s*(Modifier[^,]*),\s*(Arrangement\.[^,]+),\s*(PaddingValues\([^)]+\)),?\s*\)', 
                     r'Lazy\1(\2, verticalArrangement = \3, contentPadding = \4)' if 'Column' in r'\1' else r'Lazy\1(\2, horizontalArrangement = \3, contentPadding = \4)', content)

    # Specifically fix RoadmapScreen Column
    content = content.replace("Alignment.CenterHorizontally, Arrangement.spacedBy(3.dp),", "horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp),")
    content = content.replace("Arrangement.spacedBy(12.dp), Alignment.CenterVertically,", "horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,")

    # Write back
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)


# Wait, instead of regex, I'll just write a script that does literal replacements based on the compiler errors.
