package dev.neohub.service;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReadmeParserService {

    // Extract install section from README
    public String extractInstallGuide(String readme) {
        if (readme == null)
            return null;
        return extractSection(readme,
                "(?i)(installation|installing|install|setup|getting started)",
                "(?i)(configuration|config|setup|usage|requirements|features|screenshots)");
    }

    // Extract configuration/usage section
    public String extractConfigExample(String readme) {
        if (readme == null)
            return null;
        return extractSection(readme,
                "(?i)(configuration|config|setup|usage)",
                "(?i)(features|screenshots|contributing|license|credits|changelog)");
    }

    // Extract keymaps section
    public String extractKeymaps(String readme) {
        if (readme == null)
            return null;
        return extractSection(readme,
                "(?i)(keymaps|key mappings|keybindings|key bindings|mappings|shortcuts)",
                "(?i)(configuration|contributing|license|credits)");
    }

    private String extractSection(String readme,
            String startPattern,
            String endPattern) {
        String[] lines = readme.split("\n");
        StringBuilder section = new StringBuilder();
        boolean inSection = false;
        int headerLevel = 0;

        for (String line : lines) {
            // Detect markdown headers
            if (line.startsWith("#")) {
                int level = 0;
                while (level < line.length() && line.charAt(level) == '#')
                    level++;
                String title = line.substring(level).trim();

                if (!inSection && title.matches(startPattern)) {
                    inSection = true;
                    headerLevel = level;
                    section.append(line).append("\n");
                    continue;
                }

                if (inSection && level <= headerLevel
                        && title.matches(endPattern)) {
                    break;
                }
            }

            if (inSection) {
                section.append(line).append("\n");
            }
        }

        String result = section.toString().trim();
        // Limit to 3000 chars to keep DB clean
        return result.isEmpty() ? null
                : result.substring(0, Math.min(result.length(), 3000));
    }
}