package org.incode.eurocommercial.relatio.dom.event;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.joda.time.LocalDate;

import org.apache.isis.applib.value.Blob;

import org.incode.eurocommercial.relatio.dom.aspect.Aspect;
import org.incode.eurocommercial.relatio.dom.aspect.AspectType;
import org.incode.eurocommercial.relatio.dom.utils.DateFormatUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EventSourceType {

    WifiProjects_Utenti_Csv(new WifiProjectsUtentiCsv()),
    WifiProjects_Accessi_Csv(new WifiProjectsAccessiCsv()),
    ContestOnline_2017_Csv(new ContestOnline2017Csv()),
    Newsletter_Online_Contest_Csv(new NewsletterOnlineContestCsv()),
    Database_Wifi_2018_Csv(new DatabaseWifi2018Csv()),
    Moduli_Privacy_Presso_Infopoint_Csv(new ModuliPrivacyPressoInfopointCsv()),
    Moduli_Privacy_Abissi_Csv(new ModuliPrivacyAbissiCsv()),
    Couponing_Da_Infopad_Csv(new CouponingDaInfopadCsv()),
    Carosello_Angry_Birds_Csv(new CaroselloAngryBirdsCsv()),
    Anagrafiche_Gadget_Carosello_Csv(new AnagraficheGadgetCaroselloCsv()),
    Anagrafiche_Csv(new AnagraficheCsv()),
    Wifi_Old_Csv(new WifiOldCsv()),
    Infopoint_Csv(new InfoPointCsv());

    @Getter
    private EventParser parser;

    public EventSource parseBlob(final Blob blob, final EventRepository eventRepository, final EventSourceRepository eventSourceRepository) {
        EventSource source = eventSourceRepository.create(this);

        try {
            ByteArrayInputStream is = new ByteArrayInputStream(blob.getBytes());
            BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));

            /* Skip header lines (TODO: generalise this, since its csv-specific) */
            for (int i = 0;  i < Objects.requireNonNull(getParser()).headerSize(); i++) {
                bfReader.readLine();
            }
            for (String record = bfReader.readLine(); record != null; record = bfReader.readLine()) {
                final long time = System.nanoTime();
                final Event event = eventRepository.create(source, record);
                for (Aspect aspect : event.getAspects()) {
                    if (aspect.getProfile() != null) {
                        aspect.getType().updateProfile(aspect);
                    }
                }
                final double elapsedTime = (System.nanoTime() - time) / 1e9;
                System.out.println("==========================> Elapsed time: " + elapsedTime);
            }
            source.setStatus(EventSource.Status.SUCCESS);
        } catch (IOException e) {
            source.setStatus(EventSource.Status.FAILURE);
            e.printStackTrace();
        }
        return source;
    }


    public interface EventParser {
        Map<AspectType, String> toMap(String data);
        int headerSize();
    }

    public interface EventParserForCsv extends EventParser {
        String header();
        String separator();
    }


    public static class WifiProjectsUtentiCsv implements EventParserForCsv {
        public String header() {
            return "NOME;COGNOME;DATA PRIMO ACCESSO;EMAIL;TELEFONO;ACCESSO SOCIAL;DATI SOCIAL";
        }

        public int headerSize() {
            return 1;
        }

        @Override public String separator() {
            return ";";
        }

        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                //https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes/1757107#1757107
                final String[] values = data.split(separator());
                map.put(AspectType.FirstName, values[0]);
                map.put(AspectType.LastName, values[1]);
                map.put(AspectType.Access, DateFormatUtils.toISOLocalDate(values[2], "dd/MM/yyyy"));
                map.put(AspectType.EmailAccount, values[3]);
                map.put(AspectType.CellPhoneNumber, values[4]);
                try {
                    final SocialAccount socialAccount = SocialAccount.valueOfNormalised(values[5]);
                    map.putAll(socialAccount.toMap(values[6]));
                } catch (Exception e){
                    //
                }
            } catch (ArrayIndexOutOfBoundsException e) {

            }

            return map;
        }

        @AllArgsConstructor
        public enum SocialAccount{
            Twitter(AspectType.TwitterAccount) {
                @Override Map<AspectType, String> toMap(final String input) {
                    return new HashMap<>();
                }
            },
            Facebook(AspectType.FacebookAccount) {
                @Override Map<AspectType, String> toMap(final String input) {
                    final Map<String, String> stringMap = Splitter.on('&').withKeyValueSeparator('=').split(input);
                    Map<AspectType, String> map = Maps.newHashMap();
                    map.put(AspectType.FacebookAccount, stringMap.get("userid"));
                    map.put(AspectType.FirstName, stringMap.get("first_name"));
                    map.put(AspectType.LastName, stringMap.get("last_name"));
                    map.put(AspectType.Birthday, stringMap.get("birthday"));
                    map.put(AspectType.Gender, stringMap.get("gender"));
                    // TODO: We see age_range, what to do with it?
                    return map;
                }
            },
            GooglePlus(AspectType.GooglePlusAccount) {
                @Override Map<AspectType, String> toMap(final String input) {
                    final Map<String, String> stringMap = Splitter.on('&').withKeyValueSeparator('=').split(input);
                    Map<AspectType, String> map = Maps.newHashMap();
                    map.put(AspectType.GooglePlusAccount, stringMap.get("userid"));
                    map.put(AspectType.FirstName, stringMap.get("first_name"));
                    map.put(AspectType.LastName, stringMap.get("last_name"));
                    map.put(AspectType.Gender, stringMap.get("gender"));
                    return map;
                }
            },
            LinkedIn(AspectType.LinkedInAccount) {
                @Override Map<AspectType, String> toMap(final String input) {
                    final Map<String, String> stringMap = Maps.newHashMap();
                    String[] pairs = input.split("&");
                    for (String pair : pairs) {
                        String[] parts = pair.split("=", 1);
                        stringMap.put(parts[0], parts[1]);
                    }
                    Map<AspectType, String> map = Maps.newHashMap();
                    map.put(AspectType.LinkedInAccount, stringMap.get("userid"));
                    return map;
                }
            },
            SMS(AspectType.CellPhoneNumber) {
                @Override Map<AspectType, String> toMap(final String input) {
                    return Collections.emptyMap();
                }
            };

            @Getter
            private AspectType aspectType;

            abstract Map<AspectType, String> toMap(String input);

            static SocialAccount valueOfNormalised(final String name){
                return SocialAccount.valueOf(name.replace("+", "Plus"));
            }

        }
    }

    public static class WifiProjectsAccessiCsv implements EventParserForCsv {
        public String header() {
            return "ID;USERNAME;MAC-ADDR.;DATA ACCESSO;DATA FINE;ACCESSO;NOTE";
        }

        public int headerSize() {
            return 1;
        }

        @Override public String separator() {
            return ";";
        }

        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());
                map.put(AspectType.MacAddress, values[2]);
                map.put(AspectType.Access, DateFormatUtils.toISOLocalDateTime(values[3], "yyyy-MM-dd HH:mm:ss"));
                map.putAll(Accesso.from(values));
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }

            return map;
        }

        public enum Accesso {
            FB() {
                @Override Map<AspectType, String> toMap(final String[] input) {
                    Map<AspectType, String> map = Maps.newHashMap();
                    map.put(AspectType.EmailAccount, input[1]);
                    return map;
                }
            },
            SMS {
                @Override Map<AspectType, String> toMap(final String[] input) {
                    Map<AspectType, String> map = Maps.newHashMap();
                    map.put(AspectType.CellPhoneNumber, input[1]);
                    return map;
                }
            };

            abstract Map<AspectType, String> toMap(String[] input);

            public static Map<AspectType, String> from(final String[] values) {
                try {
                    return Accesso.valueOf(values[5]).toMap(values);
                } catch (IllegalArgumentException e) {
                    //TODO cannot parse
                    return new HashMap<>();
                }
            }
        }
    }

    public static class ContestOnline2017Csv implements EventParserForCsv {
        public String header() {
            return "nome;cognome;sesso;nascita;email;cellulare;via;citta;civico;cap;provincia;stato_famiglia;hafigli;privacy;data_ins;data_conferma_mail;data_conferma_cell;stato_mail;facebook_id;accetta comunicazioni marketing;accetta profilazione";
        }

        public int headerSize() {
            return 1;
        }

        @Override public String separator() {
            return ";";
        }

        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.replaceAll("\\[NULL]", "").replaceAll("\\{ }", "").split(separator());
                map.put(AspectType.FirstName, values[0]);
                map.put(AspectType.LastName, values[1]);
                map.put(AspectType.EmailAccount, values[4]);
                map.put(AspectType.CellPhoneNumber, values[5]);
                map.put(AspectType.City, values[7]);
                if(values[13].trim().equals("1")) {
                    map.put(AspectType.HasReadPrivacyPolicy, "true");
                }
                map.put(AspectType.RegisteredAt, DateFormatUtils.toISOLocalDateTime(values[14], "MMM d yyyy HH:mm:ss:SSSa"));
                map.put(AspectType.MailConfirmedAt, DateFormatUtils.toISOLocalDateTime(values[14], "MMM d yyyy HH:mm:ss:SSSa"));
                map.put(AspectType.FacebookAccount, values[18]);
                if(values[19].trim().equals("1")) {
                    map.put(AspectType.HasGivenMarketingConsent, "true");
                }
            } catch (ArrayIndexOutOfBoundsException ignored) { }

            return map;
        }
    }

    public static class InfoPointCsv implements EventParserForCsv {
        public String header() {
            return "NOME;COGNOME;DATA DI NASCITA;INDIRIZZO;CITTA';Country;PV;CAP;TELEFONO;CELL.;E-MAIL ;CONSENSO II";
        }

        public int headerSize() {
            return 1;
        }

        @Override public String separator() {
            return ";";
        }

        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());
                map.put(AspectType.FirstName, values[0]);
                map.put(AspectType.LastName, values[1]);
                try {
                    map.put(AspectType.Birthday, DateFormatUtils.toISOLocalDate(values[2], "dd/MM/yyyy"));
                } catch (Exception ignored) {
                }
                map.put(AspectType.Address, values[3]);
                map.put(AspectType.City, values[4]);
                map.put(AspectType.Country, values[5]);
                map.put(AspectType.PostCode, values[7]);
                map.put(AspectType.HomePhoneNumber, values[8]);
                map.put(AspectType.CellPhoneNumber, values[9]);
                map.put(AspectType.EmailAccount, values[10]);
                if(values[11].trim().equals("SI")) {
                    map.put(AspectType.HasGivenMarketingConsent, "true");
                }
            } catch (ArrayIndexOutOfBoundsException ignored) { }

            return map;
        }
    }

    public static class NewsletterOnlineContestCsv implements EventParserForCsv {
        public String header() { return null; } //has no header
        public int headerSize() {
            return 0;
        }

        @Override public String separator() {
            return ";";
        }

        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());
                map.put(AspectType.Address, values[0]);
                map.put(AspectType.EmailAccount, values[1]);
                map.put(AspectType.City, values[2]);
                map.put(AspectType.FirstName, values[3]);
                map.put(AspectType.LastName, values[4]);
            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class DatabaseWifi2018Csv implements EventParserForCsv {
        public String header() {
            return "MOME;COGNOME;DATA PRIMO ACCESSO;EMAIL;TELEFONO;RANGE ETA';SESSO;ACCESSO SOCIAL";
        }
        public int headerSize() {
            return 1;
        }
        @Override public String separator() {
            return ";";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());

                map.put(AspectType.FirstName, values[0]);
                map.put(AspectType.LastName, values[1]);
                map.put(AspectType.Access, DateFormatUtils.toISOLocalDate(values[2], "dd/MM/yyyy"));
                map.put(AspectType.EmailAccount, values[3]);
                map.put(AspectType.CellPhoneNumber, values[4]);
                //map.put(AspectType.AgeRange, values[5]);
                if(values[6].equals("M")) {
                    map.put(AspectType.Gender, "MALE");
                }
                if(values[6].equals("F")) {
                    map.put(AspectType.Gender, "FEMALE");
                }
                //map.put(AspectType.HasFacebook, values[7]);

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class ModuliPrivacyPressoInfopointCsv implements EventParserForCsv {
        public String header() {
            return "MODULI PRIVACY PRESSO L'INFO POINT;;;;;;;;;;;;;\n" +
                    ";;;;;;;;;;;;;\n" +
                    ";;;;;;;;;;;;;\n" +
                    "NOME;;;COGNOME;;;CITTA;;;INDIRIZZO MAIL;;;;";
        }
        public int headerSize() {
            return 4;
        }
        @Override public String separator() {
            return ";";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());

                map.put(AspectType.FirstName, values[0]);
                map.put(AspectType.LastName, values[3]);
                map.put(AspectType.City, values[6]);
                map.put(AspectType.EmailAccount, values[9]);

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class ModuliPrivacyAbissiCsv implements EventParserForCsv {
        public String header() {
            return "NOME;COGNOME;M o F;Età;COMUNE ;E-MAIL;";
        }
        public int headerSize() {
            return 1;
        }
        @Override public String separator() {
            return ";";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());

                map.put(AspectType.FirstName, values[0]);
                map.put(AspectType.LastName, values[1]);
                if(values[2].equals("M")) {
                    map.put(AspectType.Gender, "MALE");
                }
                if(values[2].equals("F")) {
                    map.put(AspectType.Gender, "FEMALE");
                }
                map.put(AspectType.MinimumAge, values[3]);
                map.put(AspectType.Comune, values[4]);
                map.put(AspectType.EmailAccount, values[5]);

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class CouponingDaInfopadCsv implements EventParserForCsv {
        public String header() {
            return null;
        } //has no header
        public int headerSize() {
            return 0;
        }

        @Override public String separator() {
            return ";";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());

                map.put(AspectType.Address, values[0]);
                map.put(AspectType.EmailAccount, values[1]);
                map.put(AspectType.Comune, values[2]);
                map.put(AspectType.FirstName, values[3]);
                map.put(AspectType.LastName, values[4]);

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class CaroselloAngryBirdsCsv implements EventParserForCsv {
        public String header() {
            return "DT. INSERIMENTO;COGNOME;NOME;SESSO;ETA;INDIRIZZO;CAP;CITTA;PROVINCIA;TELEFONO;EMAIL;PRIVACY;TRATTAMENTO DATI;Firma";
        }
        public int headerSize() {
            return 1;
        }
        @Override public String separator() {
            return ";";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());

                map.put(AspectType.Access, DateFormatUtils.toISOLocalDateTime(values[0], "yyyy-MM-dd HH:mm:ss Z"));
                map.put(AspectType.LastName, values[1]);
                map.put(AspectType.FirstName, values[2]);
                map.put(AspectType.Gender, values[3]);

                String ageRange = values[4];
                switch (ageRange) {
                    case "< 18":
                        break;
                    case "18 - 30":
                        map.put(AspectType.MinimumAge, "18");
                        break;
                    case "31 - 50":
                        map.put(AspectType.MinimumAge, "31");
                        break;
                    case "51 - 70":
                        map.put(AspectType.MinimumAge, "51");
                        break;
                    case "> 70":
                        map.put(AspectType.MinimumAge, "71");
                        break;
                    default:
                        break;
                }

                map.put(AspectType.Address, values[5]);
                map.put(AspectType.PostCode, values[6]);
                map.put(AspectType.City, values[7]);
                map.put(AspectType.Province, values[8]);
                map.put(AspectType.HomePhoneNumber, values[9]);
                map.put(AspectType.EmailAccount, values[10]);
                if(values[11].equals("Consento")) {
                    map.put(AspectType.HasReadPrivacyPolicy, "true");
                }
                if(values[12].equals("Consento")) {
                    map.put(AspectType.HasGivenMarketingConsent, "true");
                }
                //values[13]; jpg filename refers to scanned signature

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class AnagraficheGadgetCaroselloCsv implements EventParserForCsv {
        LocalDate currentDate;

        public String header() {
            return "GIORNO ;AURICOLARI ;GETTONE X CARRELLO ;SUPPORTO CEL;MAIL";
        }
        public int headerSize() {
            return 1;
        }
        @Override public String separator() {
            return ";";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());

                String format = "dd/MM/yy";
                LocalDate result = DateFormatUtils.parseStringToLocalDateOrNull(values[0], format);

                //current row is a user
                if(result == null) {

                    //currentDate not set yet
                    if(currentDate != null) {
                        map.put(AspectType.Access, currentDate.toString());
                        //todo: inform/log
                    }

                    //could be split into first/lastname, but data ordering is inconsistent
                    map.put(AspectType.FullName, values[0]);
                    map.put(AspectType.EmailAccount, values[4]);

                    if(!Strings.isNullOrEmpty(values[1])) {
                        map.put(AspectType.Belongings, "AURICOLARI");
                    }
                    //unsure how to handle "GETTONE X CARRELLO", "SUPPORTO CELL"
                }

                //current row is a date
                else {
                    currentDate = result;
                }

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class AnagraficheCsv implements EventParserForCsv {
        public String header() {
            return "Data tesseramento\tData ultima visita\tCodice\tCognome\tNome\tRagione Sociale\tIndirizzo\tLocalita\tCAP\tProvincia\tTelefono\tData di Nascita\tEMail\tConsenso\tConsenso Marketing";
        }
        public int headerSize() {
            return 1;
        }
        @Override public String separator() {
            return "\t";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator());

                map.put(AspectType.RegisteredAt, DateFormatUtils.toISOLocalDate(values[0],"dd/MM/yyyy"));
                map.put(AspectType.Access, DateFormatUtils.toISOLocalDate(values[1],"dd/MM/yyyy"));
                //Codice: code
                map.put(AspectType.LastName, values[3]);
                map.put(AspectType.FirstName, values[4]);
                map.put(AspectType.BusinessName, values[5]);
                map.put(AspectType.Address, values[6]);
                map.put(AspectType.Localita, values[7]);
                map.put(AspectType.PostCode, values[8]);
                map.put(AspectType.Province, values[9]);
                map.put(AspectType.HomePhoneNumber, values[10]);
                map.put(AspectType.Birthday, DateFormatUtils.toISOLocalDate(values[11],"dd/MM/yyyy"));
                map.put(AspectType.EmailAccount, values[12]);
                if(values[13].trim().equals("SI")) {
                    map.put(AspectType.HasReadPrivacyPolicy, "true");
                }
                if(values[14].trim().equals("SI")) {
                    map.put(AspectType.HasGivenMarketingConsent, "true");
                }

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }

    public static class WifiOldCsv implements EventParserForCsv {
        public String header() {
            return "auth_method;social_action;created_at;user_first_name;user_last_name;user_picture;user_gender;user_email;user_phone;user_birthday;user_location_city;user_location_country;user_auth_method;user_auth_user_id;user_original_mac_address;user_registered_at;user_last_login";
        }
        public int headerSize() {
            return 1;
        }
        @Override public String separator() {
            return ";";
        }
        @Override
        public Map<AspectType, String> toMap(String data) {
            Map<AspectType, String> map = Maps.newHashMap();

            try {
                final String[] values = data.split(separator() + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                String auth_method = values[0];
                switch (auth_method) {
                    case "google":
                        //storing email address for google users, id is malformed
                        map.put(AspectType.GooglePlusAccount, values[7]);
                        break;
                    case "facebook":
                        map.put(AspectType.FacebookAccount, values[5].replaceAll("[^0-9]",""));
                        break;
                    case "live":
                        map.put(AspectType.LiveAccount, values[13]);
                        break;
                    case "linkedin":
                        map.put(AspectType.LinkedInAccount, values[13]);
                        break;
                    case "twitter":
                        map.put(AspectType.TwitterAccount, values[13]);
                        break;
                    case "instagram":
                        map.put(AspectType.InstagramAccount, values[13]);
                        break;
                    case "sms":
                        break;
                    default:
                        break;
                }

                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

                //values[1]: {"type":"like","page_id":"128931679729","action":1}
                //values[2]: 'created at', presumably when the record was created
                map.put(AspectType.FirstName, values[3]);
                map.put(AspectType.LastName, values[4]);
                //values[5]: 'user-picture' url
                map.put(AspectType.Gender, values[6]);
                map.put(AspectType.EmailAccount, values[7]);
                //values[8]: 'user-phone' useless: only 1 entry, malformed

                try {
                    map.put(AspectType.Birthday,
                        java.time.LocalDateTime.parse(values[9], formatter)
                            .toLocalDate().toString());
                }
                catch(Exception e) {}

                map.put(AspectType.City, values[10]); //'user_location_city' likely does not refer to residence, but location.
                map.put(AspectType.Country, values[11]); //similar to ^
                //values[12]: duplicate of values[0], but blank where values[0] == twitter
                //values[13]: user-id, invalid for google,facebook,sms
                map.put(AspectType.MacAddress, values[14]);
                map.put(AspectType.RegisteredAt, java.time.LocalDateTime.parse(values[15], formatter).toString());
                map.put(AspectType.Access, java.time.LocalDateTime.parse(values[16], formatter).toString());

            } catch (ArrayIndexOutOfBoundsException e) {}

            return map;
        }
    }
}
