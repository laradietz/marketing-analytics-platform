package com.marketinganalytics.platform.config;

import com.marketinganalytics.platform.entity.*;
import com.marketinganalytics.platform.entity.enums.*;
import com.marketinganalytics.platform.repository.*;
import com.marketinganalytics.platform.security.SecurityUser;
import com.marketinganalytics.platform.service.LeadScoringService;
import com.marketinganalytics.platform.service.RecommendationService;
import com.marketinganalytics.platform.service.ai.AiContentProvider;
import com.marketinganalytics.platform.service.ai.AiGenerationContext;
import com.marketinganalytics.platform.service.sentiment.SentimentClassifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Populates a fresh development database with a coherent demo dataset:
 * one user, a handful of campaigns across every platform with 14 days of
 * metrics whose spend/revenue are chosen so ROAS matches realistic,
 * hand-picked values, plus leads and content. Gated by
 * {@code app.seed.enabled} (default true) so it never runs against a
 * production database by accident.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMetricRepository campaignMetricRepository;
    private final DailyMetricRepository dailyMetricRepository;
    private final LeadRepository leadRepository;
    private final ContentRepository contentRepository;
    private final ContentIdeaRepository contentIdeaRepository;
    private final GoalRepository goalRepository;
    private final AudienceCommentRepository audienceCommentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AiContentProvider aiContentProvider;
    private final SentimentClassifier sentimentClassifier;
    private final RecommendationService recommendationService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.demo-user-email:demo@marketinganalytics.com}")
    private String demoEmail;

    @Value("${app.seed.demo-user-password:Demo1234!}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(demoEmail)) {
            log.info("Demo data already present, skipping seed.");
            return;
        }

        log.info("Seeding demo data...");

        User admin = userRepository.save(User.builder()
                .name("Lara Dietz")
                .email(demoEmail)
                .passwordHash(passwordEncoder.encode(demoPassword))
                .role(Role.ADMIN)
                .jobTitle("Marketing Manager")
                .companyName("Nimbus Studio")
                .active(true)
                .build());

        User analyst = userRepository.save(User.builder()
                .name("Martín Ibarra")
                .email("martin.ibarra@marketinganalytics.com")
                .passwordHash(passwordEncoder.encode("Analista1234!"))
                .role(Role.USER)
                .jobTitle("Growth Analyst")
                .companyName("Nimbus Studio")
                .active(true)
                .build());

        Map<String, Platform> platforms = new HashMap<>();
        platformRepository.findAll().forEach(p -> platforms.put(p.getSlug(), p));

        Campaign instagram = createCampaign("Lanzamiento Colección Primavera", platforms.get("instagram"),
                CampaignObjective.SALES, new BigDecimal("150000"), CampaignStatus.ACTIVE, admin, 45,
                "Mujeres y hombres de 25-40 años interesados en moda sustentable");
        Campaign facebook = createCampaign("Reconquista de clientes inactivos", platforms.get("facebook"),
                CampaignObjective.CONVERSIONS, new BigDecimal("90000"), CampaignStatus.ACTIVE, analyst, 30,
                "Clientes existentes sin compras en los últimos 6 meses");
        Campaign google = createCampaign("Búsquedas de alta intención - Q3", platforms.get("google-ads"),
                CampaignObjective.TRAFFIC, new BigDecimal("120000"), CampaignStatus.ACTIVE, admin, 60,
                "Usuarios buscando activamente productos de la categoría");
        Campaign tiktok = createCampaign("Serie de contenido viral #NimbusChallenge", platforms.get("tiktok"),
                CampaignObjective.ENGAGEMENT, new BigDecimal("60000"), CampaignStatus.PAUSED, analyst, 20,
                "Gen Z, 16-24 años, usuarios activos de TikTok");
        Campaign linkedin = createCampaign("Generación de leads B2B", platforms.get("linkedin"),
                CampaignObjective.LEADS, new BigDecimal("70000"), CampaignStatus.ACTIVE, admin, 40,
                "Tomadores de decisión en empresas medianas del rubro retail");
        Campaign draft = createCampaign("Campaña Black Friday (borrador)", platforms.get("instagram"),
                CampaignObjective.AWARENESS, new BigDecimal("30000"), CampaignStatus.DRAFT, analyst, 5, null);
        Campaign completed = createCampaign("Día del Padre 2025", platforms.get("facebook"),
                CampaignObjective.SALES, new BigDecimal("40000"), CampaignStatus.COMPLETED, admin, 90,
                "Compradores de regalos, hombres y mujeres 25-55 años");

        seedMetrics(instagram, new BigDecimal("100000"), new BigDecimal("420000"), 2_000_000, 48_000, 1600,
                15_000, 800, 600, 1200);
        seedMetrics(facebook, new BigDecimal("80000"), new BigDecimal("136000"), 1_500_000, 27_000, 900,
                8_000, 400, 300, 500);
        seedMetrics(google, new BigDecimal("100000"), new BigDecimal("350000"), 1_800_000, 54_000, 2000,
                0, 0, 0, 0);
        seedMetrics(tiktok, new BigDecimal("40000"), new BigDecimal("44000"), 900_000, 27_000, 500,
                20_000, 1500, 2200, 1800);
        seedMetrics(linkedin, new BigDecimal("50000"), new BigDecimal("45000"), 300_000, 4_500, 150,
                900, 120, 60, 90);
        seedMetrics(completed, new BigDecimal("38000"), new BigDecimal("91200"), 700_000, 12_000, 480,
                3000, 150, 90, 200);

        refreshDailyMetricRollups();

        seedLeads(instagram, linkedin, admin, analyst);
        seedContent(platforms, instagram, google, admin, analyst);
        seedContentIdeas(platforms, admin);
        seedGoals(instagram, admin);
        seedComments(platforms);

        authenticateAs(admin);
        try {
            recommendationService.generate();
        } finally {
            SecurityContextHolder.clearContext();
        }

        log.info("Demo data seeded. Login with {} / {}", demoEmail, demoPassword);
    }

    private Campaign createCampaign(String name, Platform platform, CampaignObjective objective, BigDecimal budget,
                                     CampaignStatus status, User owner, int daysAgoStart, String audience) {
        Campaign campaign = Campaign.builder()
                .name(name)
                .description("Campaña de " + objective.name().toLowerCase() + " en " + platform.getName())
                .platform(platform)
                .objective(objective)
                .budget(budget)
                .startDate(LocalDate.now().minusDays(daysAgoStart))
                .endDate(status == CampaignStatus.COMPLETED ? LocalDate.now().minusDays(daysAgoStart - 60) : null)
                .status(status)
                .targetAudience(audience)
                .owner(owner)
                .notes(null)
                .build();
        return campaignRepository.save(campaign);
    }

    private void seedMetrics(Campaign campaign, BigDecimal totalSpend, BigDecimal totalRevenue, long totalImpressions,
                              long totalClicks, long totalConversions, long totalLikes, long totalComments,
                              long totalShares, long totalSaves) {
        int days = 14;
        BigDecimal[] spendPerDay = distributeMoney(totalSpend, days);
        BigDecimal[] revenuePerDay = distributeMoney(totalRevenue, days);
        long[] impressionsPerDay = distributeLong(totalImpressions, days);
        long[] clicksPerDay = distributeLong(totalClicks, days);
        long[] conversionsPerDay = distributeLong(totalConversions, days);
        long[] likesPerDay = distributeLong(totalLikes, days);
        long[] commentsPerDay = distributeLong(totalComments, days);
        long[] sharesPerDay = distributeLong(totalShares, days);
        long[] savesPerDay = distributeLong(totalSaves, days);

        List<CampaignMetric> metrics = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(days - 1L - i);
            metrics.add(CampaignMetric.builder()
                    .campaign(campaign)
                    .recordedDate(date)
                    .impressions(impressionsPerDay[i])
                    .reach((long) (impressionsPerDay[i] * 0.8))
                    .clicks(clicksPerDay[i])
                    .conversions(conversionsPerDay[i])
                    .spend(spendPerDay[i])
                    .revenue(revenuePerDay[i])
                    .likes(likesPerDay[i])
                    .comments(commentsPerDay[i])
                    .shares(sharesPerDay[i])
                    .saves(savesPerDay[i])
                    .build());
        }
        campaignMetricRepository.saveAll(metrics);
    }

    private BigDecimal[] distributeMoney(BigDecimal total, int parts) {
        BigDecimal[] result = new BigDecimal[parts];
        BigDecimal base = total.divide(BigDecimal.valueOf(parts), 2, RoundingMode.DOWN);
        BigDecimal accumulated = BigDecimal.ZERO;
        for (int i = 0; i < parts - 1; i++) {
            result[i] = base;
            accumulated = accumulated.add(base);
        }
        result[parts - 1] = total.subtract(accumulated);
        return result;
    }

    private long[] distributeLong(long total, int parts) {
        long[] result = new long[parts];
        long base = total / parts;
        long remainder = total - (base * parts);
        Arrays.fill(result, base);
        result[parts - 1] += remainder;
        return result;
    }

    private void refreshDailyMetricRollups() {
        List<CampaignMetric> all = campaignMetricRepository.findAll();
        Map<String, DailyMetric> rollups = new LinkedHashMap<>();

        for (CampaignMetric m : all) {
            Platform platform = m.getCampaign().getPlatform();
            String key = m.getRecordedDate() + "|" + platform.getId();
            DailyMetric rollup = rollups.computeIfAbsent(key, k -> DailyMetric.builder()
                    .metricDate(m.getRecordedDate())
                    .platform(platform)
                    .impressions(0).clicks(0).conversions(0)
                    .spend(BigDecimal.ZERO).revenue(BigDecimal.ZERO)
                    .build());
            rollup.setImpressions(rollup.getImpressions() + m.getImpressions());
            rollup.setClicks(rollup.getClicks() + m.getClicks());
            rollup.setConversions(rollup.getConversions() + m.getConversions());
            rollup.setSpend(rollup.getSpend().add(m.getSpend()));
            rollup.setRevenue(rollup.getRevenue().add(m.getRevenue()));
        }

        dailyMetricRepository.saveAll(rollups.values());
    }

    private void seedLeads(Campaign instagram, Campaign linkedin, User admin, User analyst) {
        record LeadSeed(String name, String email, String company, LeadSource source, LeadStatus status,
                         int contactCount, Campaign campaign, User assignedTo) {
        }

        List<LeadSeed> seeds = List.of(
                new LeadSeed("Camila Rossi", "camila.rossi@example.com", "Rossi Diseño", LeadSource.SOCIAL_MEDIA, LeadStatus.NEW, 0, instagram, null),
                new LeadSeed("Julián Torres", "julian.torres@example.com", "Torres Consultora", LeadSource.REFERRAL, LeadStatus.QUALIFIED, 3, linkedin, admin),
                new LeadSeed("Sofía Medina", "sofia.medina@example.com", null, LeadSource.PAID_ADS, LeadStatus.CONTACTED, 1, instagram, analyst),
                new LeadSeed("Bruno Castro", "bruno.castro@example.com", "Castro Retail", LeadSource.EVENT, LeadStatus.CONVERTED, 4, linkedin, admin),
                new LeadSeed("Valentina Ruiz", "valentina.ruiz@example.com", null, LeadSource.ORGANIC, LeadStatus.NEW, 0, null, null),
                new LeadSeed("Nicolás Peralta", "nicolas.peralta@example.com", "Peralta S.A.", LeadSource.REFERRAL, LeadStatus.QUALIFIED, 2, linkedin, analyst),
                new LeadSeed("Agustina Vega", "agustina.vega@example.com", null, LeadSource.SOCIAL_MEDIA, LeadStatus.CONTACTED, 1, instagram, null),
                new LeadSeed("Tomás Fernández", "tomas.fernandez@example.com", "Fernández Hnos.", LeadSource.EMAIL, LeadStatus.LOST, 2, null, admin),
                new LeadSeed("Micaela López", "micaela.lopez@example.com", null, LeadSource.OTHER, LeadStatus.NEW, 0, null, null),
                new LeadSeed("Franco Giménez", "franco.gimenez@example.com", "Giménez Corp", LeadSource.EVENT, LeadStatus.QUALIFIED, 3, linkedin, admin)
        );

        for (LeadSeed s : seeds) {
            Lead lead = Lead.builder()
                    .name(s.name()).email(s.email()).company(s.company())
                    .source(s.source()).status(s.status()).contactCount(s.contactCount())
                    .campaign(s.campaign()).assignedTo(s.assignedTo())
                    .build();
            lead.setScore(LeadScoringService.computeScore(lead));
            leadRepository.save(lead);
        }
    }

    private void seedContent(Map<String, Platform> platforms, Campaign instagram, Campaign google, User admin, User analyst) {
        contentRepository.save(Content.builder()
                .title("Detrás de escena: nueva colección")
                .copyText("Te mostramos cómo se hizo nuestra última colección, paso a paso.")
                .platform(platforms.get("instagram"))
                .objective(CampaignObjective.ENGAGEMENT)
                .status(ContentStatus.PUBLISHED)
                .scheduledDate(LocalDate.now().minusDays(5))
                .publishedDate(LocalDate.now().minusDays(5))
                .hashtags("#nimbusstudio #coleccion #hechoamano")
                .ctaText("Ver más")
                .campaign(instagram)
                .createdBy(admin)
                .build());

        contentRepository.save(Content.builder()
                .title("Testimonios de clientes satisfechos")
                .copyText("Nuestros clientes cuentan su experiencia con Nimbus Studio.")
                .platform(platforms.get("facebook"))
                .objective(CampaignObjective.CONVERSIONS)
                .status(ContentStatus.SCHEDULED)
                .scheduledDate(LocalDate.now().plusDays(2))
                .hashtags("#testimonios #clientesfelices")
                .ctaText("Conocé más")
                .createdBy(analyst)
                .build());

        contentRepository.save(Content.builder()
                .title("Guía rápida: cómo elegir el producto ideal")
                .copyText("Una guía breve para ayudar a nuestra audiencia a decidir mejor.")
                .platform(platforms.get("google-ads"))
                .objective(CampaignObjective.TRAFFIC)
                .status(ContentStatus.DRAFT)
                .scheduledDate(LocalDate.now().plusDays(5))
                .ctaText("Leer guía")
                .campaign(google)
                .createdBy(admin)
                .build());

        contentRepository.save(Content.builder()
                .title("Reto viral de la semana")
                .copyText("Sumate al challenge y ganá productos exclusivos.")
                .platform(platforms.get("tiktok"))
                .objective(CampaignObjective.ENGAGEMENT)
                .status(ContentStatus.IDEA)
                .hashtags("#challenge #viral")
                .createdBy(analyst)
                .build());

        contentRepository.save(Content.builder()
                .title("Caso de éxito: Retail Corp")
                .copyText("Cómo ayudamos a Retail Corp a triplicar sus conversiones.")
                .platform(platforms.get("linkedin"))
                .objective(CampaignObjective.LEADS)
                .status(ContentStatus.SCHEDULED)
                .scheduledDate(LocalDate.now().plusDays(7))
                .ctaText("Agendar una llamada")
                .createdBy(admin)
                .build());

        contentRepository.save(Content.builder()
                .title("Novedades de la temporada")
                .copyText("Descubrí todo lo nuevo antes que nadie.")
                .platform(platforms.get("instagram"))
                .objective(CampaignObjective.AWARENESS)
                .status(ContentStatus.PUBLISHED)
                .scheduledDate(LocalDate.now().minusDays(1))
                .publishedDate(LocalDate.now().minusDays(1))
                .hashtags("#novedades #temporada")
                .ctaText("Ver colección")
                .createdBy(analyst)
                .build());
    }

    private void seedContentIdeas(Map<String, Platform> platforms, User admin) {
        AiGenerationContext context = new AiGenerationContext(
                "línea de indumentaria sustentable",
                "jóvenes profesionales interesados en moda consciente",
                platforms.get("instagram").getName(),
                CampaignObjective.SALES,
                ContentTone.ENERGETIC,
                null,
                2
        );

        var variants = aiContentProvider.generate(context);
        for (var variant : variants) {
            contentIdeaRepository.save(ContentIdea.builder()
                    .productOrService(context.productOrService())
                    .targetAudience(context.targetAudience())
                    .platform(platforms.get("instagram"))
                    .objective(context.objective())
                    .tone(context.tone())
                    .generatedTitle(variant.title())
                    .generatedCopy(variant.copy())
                    .generatedCta(variant.callToAction())
                    .generatedHashtags(String.join(" ", variant.hashtags()))
                    .createdBy(admin)
                    .build());
        }
    }

    private void seedGoals(Campaign instagram, User admin) {
        goalRepository.save(Goal.builder()
                .name("ROAS objetivo Instagram - trimestre")
                .metricType(GoalMetric.ROAS)
                .targetValue(new BigDecimal("4.50"))
                .periodStart(LocalDate.now().minusDays(45))
                .periodEnd(LocalDate.now().plusDays(45))
                .campaign(instagram)
                .createdBy(admin)
                .build());

        goalRepository.save(Goal.builder()
                .name("Nuevos leads del mes")
                .metricType(GoalMetric.LEADS)
                .targetValue(new BigDecimal("25"))
                .periodStart(LocalDate.now().minusDays(15))
                .periodEnd(LocalDate.now().plusDays(15))
                .createdBy(admin)
                .build());
    }

    private void seedComments(Map<String, Platform> platforms) {
        record CommentSeed(String platformSlug, String author, String text, int daysAgo) {
        }

        // Deliberately no Google Ads comments — search ads don't have a comments
        // surface the way social platforms do, so forcing fake ones there would
        // misrepresent what the feature actually covers.
        List<CommentSeed> seeds = List.of(
                new CommentSeed("instagram", "valentina.rz", "Me encanta la calidad de la tela, superó mis expectativas", 3),
                new CommentSeed("instagram", "juanpi.moda", "El envío fue rapidísimo, llegó en dos días", 5),
                new CommentSeed("instagram", "sole.fashion", "Hermoso diseño, ya pedí otro color", 2),
                new CommentSeed("instagram", "martinaok", "La talla no coincidía con la guía de talles, un poco decepcionante", 6),
                new CommentSeed("facebook", "carlos.perez88", "Buena atención al cliente, resolvieron mi consulta rápido", 4),
                new CommentSeed("facebook", "anahi.gomez", "El precio me pareció caro comparado con la competencia", 7),
                new CommentSeed("facebook", "ricardo.lm", "Pésima experiencia, el pedido llegó incompleto", 8),
                new CommentSeed("facebook", "florencia.dg", "Excelente relación calidad-precio, lo recomiendo", 3),
                new CommentSeed("tiktok", "camigamer22", "Este challenge está buenísimo, ya lo hice con mis amigas", 1),
                new CommentSeed("tiktok", "nacho.tt", "Video genial, la edición es increíble", 2),
                new CommentSeed("tiktok", "luli.vlogs", "No entendí bien la dinámica del challenge", 4),
                new CommentSeed("linkedin", "roberto.ceo", "Excelente caso de éxito, nos gustaría agendar una demo", 2),
                new CommentSeed("linkedin", "patricia.hr", "Interesante propuesta, aunque el proceso de implementación parece lento", 5),
                new CommentSeed("linkedin", "gonzalo.ops", "Buen contenido, comparto con mi equipo de operaciones", 3)
        );

        for (CommentSeed seed : seeds) {
            Platform platform = platforms.get(seed.platformSlug());
            if (platform == null) {
                continue;
            }
            audienceCommentRepository.save(AudienceComment.builder()
                    .platform(platform)
                    .authorName(seed.author())
                    .text(seed.text())
                    .sentiment(sentimentClassifier.classify(seed.text()))
                    .source(CommentSource.MANUAL)
                    .postedAt(LocalDate.now().minusDays(seed.daysAgo()))
                    .build());
        }
    }

    private void authenticateAs(User user) {
        var securityUser = new SecurityUser(user);
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
