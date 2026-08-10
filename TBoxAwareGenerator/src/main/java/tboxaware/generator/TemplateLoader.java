package tboxaware.generator;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses templates.yaml into TemplateSpec objects. Deliberately hand-rolled from generic
 * Map/List structures (Yaml().load(...)) rather than SnakeYAML's JavaBean binding, since bean
 * binding needs getter/setter pairs and gives worse error messages on a malformed template - a
 * missing/misspelled key here should be traceable to which template broke it.
 */
public class TemplateLoader {

    @SuppressWarnings("unchecked")
    public static List<TemplateSpec> load(String path) throws Exception {
        try (InputStream in = new FileInputStream(path)) {
            Map<String, Object> root = new Yaml().load(in);
            List<Map<String, Object>> rawTemplates = (List<Map<String, Object>>) root.get("templates");
            List<TemplateSpec> result = new ArrayList<>();
            for (Map<String, Object> raw : rawTemplates) {
                result.add(parseTemplate(raw));
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private static TemplateSpec parseTemplate(Map<String, Object> raw) {
        TemplateSpec t = new TemplateSpec();
        t.id = str(raw, "id");
        t.category = str(raw, "category");
        t.phase = str(raw, "phase");
        t.frequency = raw.containsKey("frequency") ? (int) raw.get("frequency") : 1;
        t.timingStart = raw.containsKey("timingStart") ? ((Number) raw.get("timingStart")).doubleValue() : 0.0;
        t.timingEnd = raw.containsKey("timingEnd") ? ((Number) raw.get("timingEnd")).doubleValue() : 1.0;

        Map<String, Object> account = (Map<String, Object>) raw.get("account");
        t.account = new TemplateSpec.AccountSpec();
        t.account.className = str(account, "class");

        t.tweetText = str(raw, "tweetText");
        t.tweetTriples = parseTriples((List<Map<String, Object>>) raw.get("tweetTriples"));

        Map<String, Object> subj = (Map<String, Object>) raw.get("subjectResource");
        if (subj != null) {
            TemplateSpec.ResourceSpec rs = new TemplateSpec.ResourceSpec();
            rs.ns = str(subj, "ns");
            rs.localName = str(subj, "localName");
            rs.className = str(subj, "class");
            t.subjectResource = rs;
        }

        t.eventTriples = parseTriples((List<Map<String, Object>>) raw.get("eventTriples"));

        List<Map<String, Object>> rawExtras = (List<Map<String, Object>>) raw.get("extraResources");
        if (rawExtras != null) {
            for (Map<String, Object> r : rawExtras) {
                TemplateSpec.ResourceSpec rs = new TemplateSpec.ResourceSpec();
                rs.id = str(r, "id");
                rs.ns = str(r, "ns");
                rs.localName = str(r, "localName");
                rs.className = str(r, "class");
                t.extraResources.add(rs);
            }
        }
        List<Object> rawExtraUsers = (List<Object>) raw.get("extraUsers");
        if (rawExtraUsers != null) {
            for (Object id : rawExtraUsers) {
                t.extraUsers.add(id.toString());
            }
        }

        Map<String, Object> involvement = (Map<String, Object>) raw.get("userInvolvement");
        if (involvement != null) {
            TemplateSpec.UserInvolvement ui = new TemplateSpec.UserInvolvement();
            ui.role = str(involvement, "role");
            ui.roleProperty = str(involvement, "roleProperty");
            ui.linkTarget = involvement.containsKey("linkTarget") ? str(involvement, "linkTarget") : "subjectResource";
            t.userInvolvement = ui;
        }

        return t;
    }

    private static List<TemplateSpec.TripleSpec> parseTriples(List<Map<String, Object>> raw) {
        List<TemplateSpec.TripleSpec> triples = new ArrayList<>();
        if (raw == null) {
            return triples;
        }
        for (Map<String, Object> r : raw) {
            TemplateSpec.TripleSpec ts = new TemplateSpec.TripleSpec();
            ts.property = str(r, "property");
            ts.propertyNs = str(r, "propertyNs");
            ts.subjectRef = r.containsKey("subjectRef") ? str(r, "subjectRef") : "self";
            ts.literal = str(r, "literal");
            ts.resourceNs = str(r, "resourceNs");
            ts.resource = str(r, "resource");
            triples.add(ts);
        }
        return triples;
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : v.toString();
    }
}
