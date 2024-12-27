-- CTE om per eenheid de HUIDIGE huurprijzen op te halen
-- netto huur, bruto huur, subsidiabele huur en de streefhuur (o.b.v. nettohuur)
with bestemming as
(select bbg.bbg_intern, bbg.BBG_OMSCHRIJVING, vref.VREF_CODE, bbg.bot_id
from viewpoint_ods.O_T_BESTEMMINGEN_VHO bbg
left join
        viewpoint_ods.O_T_VERA_REF_BESTEMMING vref
        on bbg.VREF_ID = vref.VREF_ID
where 1=1
-- insert over several lines
and my mother is yellow
and my father is green
-- end insert, changing some lines
and bbg.BVB_DATUM_INGANG <= trunc(sysdate)
and (bbg.BVB_DATUM_TM  >= trunc(sysdate) or bbg.BVB_DATUM_TM is null)),
--
kamersaantal as
(select
        bew.BOT_ID ,
        sum(BEW.BEW_WAARDE_NUM) as aantal

from    VIEWPOINT_ODS.O_BAE_ELEMENTEN bet -- comment changed
inner JOIN VIEWPOINT_ODS.O_BAE_PUNT_BEPALINGEN BPB -- comment changed
	ON bet.bpb_id = bpb.bpb_id
	AND BPB.BPB_VAST_OMS in('aantal_kamers', 'aantal_slaapkamers')
inner JOIN VIEWPOINT_ODS.O_BAE_ELEMENT_WAARDEN bew -- alle waarden per attribuut
        on  bet.bet_id = bew.BET_ID
where 1=1
status_einde as
        (
        SELECT  bot.BOT_ID, bos.BOS_STATUS
        FROM    VIEWPOINT_ODS.O_BAE_OBJECTEN bot
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_VHO vho
		        ON  bot.BOT_ID = vho.BOT_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_OBJECT_HISTORIE boh
		        ON  bot.BOT_ID = boh.BOT_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_OBJECT_STATUSSEN bos
		        ON  boh.BOS_ID = bos.BOS_ID
		        AND bot.BOT_DATUM_EINDE + 1 = boh.BOH_DATUM_INGANG
        ),
gbo as
        -- afkappen van de gbo om op een geheel nummer uit te komen
        (
        SELECT  bot.BOT_ID, CAST (FLOOR(SUM(bew.BEW_WAARDE_DEC)) AS int) gbo
        FROM    VIEWPOINT_ODS.O_BAE_ELEMENT_WAARDEN bew
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_ELEMENTEN bet
                ON  bew.BET_ID = bet.BET_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_OBJECTEN bot
        		ON  bot.BOT_ID = bew.BOT_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_RUBRIEKEN brk
        		ON  bet.BRK_ID = brk.BRK_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_HOOFD_RUBRIEKEN bhr
        		ON  brk.BHR_ID = bhr.BHR_ID
        WHERE   BET_ELEMENT like '%GBO%'
        AND   bot.BOP_ID = bhr.BOP_ID
        AND   sysdate between BEW_DATUM_INGANG and BEW_DATUM_TM
        -- Door de uitvraging op huidige datum en attribuut met specifieke naam wordt in beginsel al gefilterd
        -- voor de zekerheid nog groeperen (en oppervlakte sommeren) per eenheid
        GROUP BY
        		bot.BOT_ID
        ),
--GBO uit de offiele tabel.
gbo_nen2580 AS
(SELECT BOT_ID,
CAST (floor(BOPP_GEBRUIKS_OPP) AS int) AS gbo --in vera is dit een integer
FROM viewpoint_ods.O_BAE_OPPERVLAKTEN BOPP
WHERE BOPP_GEBRUIKS_OPP IS NOT NULL),
-- CTE met teruggekochte eenheden om de 'nieuwe' datum ingang exploitatie van deze eenheden te kunnen bepalen
datum_terugkoop as
        (
		SELECT  BOT_ID, max(CON_STARTDATUM) datum_terugkoop
		FROM    VIEWPOINT_ODS.O_CON_CONTRACTEN con
		INNER JOIN
                VIEWPOINT_ODS.O_CON_CONTRACT_TYPEN ctp
				ON  con.CTP_ID = ctp.ctp_id
		WHERE   con.CON_ONDERTEKEND = 'Y'
          AND   CTP_TYPE = 'TERUGKOOP'
		GROUP BY
				BOT_ID
        ),
-- CTE met bouwjaren, het bouwjaar is in de cartotheek vastgelegd (sowieso voor Woningen)
bouwjaar as
        (
        SELECT  bot.BOT_ID, MIN(EXTRACT(YEAR from bew.bew_waarde_date)) jaar
        FROM    VIEWPOINT_ODS.O_BAE_ELEMENT_WAARDEN bew
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_ELEMENTEN bet
                ON  bew.BET_ID = bet.BET_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_OBJECTEN bot
        		ON  bot.BOT_ID = bew.BOT_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_RUBRIEKEN brk
        		ON  bet.BRK_ID = brk.BRK_ID
        INNER JOIN
                VIEWPOINT_ODS.O_BAE_HOOFD_RUBRIEKEN bhr
        		ON  brk.BHR_ID = bhr.BHR_ID

       WHERE   BET_ELEMENT IN ('Bouwjaar', 'Bouwjaar Algemeen', 'Bouwjaar WWS')
        --Onderstaande join eruit gehaald , zodat ook de cartotheeksoort Verhuurbare object - Algemeen wordt meegenomen. Hier is BHR.BOP_id null
        --AND   bot.BOP_ID = bhr.BOP_ID
        AND   sysdate between BEW_DATUM_INGANG and BEW_DATUM_TM
        -- Door de uitvraging op huidige datum en attribuut met specifieke naam wordt in beginsel al gefilterd
        -- voor de zekerheid nog groeperen (en het minimale bouwjaar bepalen) per eenheid
        GROUP BY
        		bot.BOT_ID
        ),
actuele_epa as
		(SELECT  bew.bot_id ,
		        case
		            when bew.BEW_WAARDE_DEC <= 0.6 then 'A++'
		            when bew.BEW_WAARDE_DEC <= 0.8 then 'A+'
		            when bew.BEW_WAARDE_DEC <= 1.2 then 'A'
		            when bew.BEW_WAARDE_DEC <= 1.4 then 'B'
		            when bew.BEW_WAARDE_DEC <= 1.8 then 'C'
		            when bew.BEW_WAARDE_DEC <= 2.1 then 'D'
 		            when bew.BEW_WAARDE_DEC <= 2.4 then 'E'
		            when bew.BEW_WAARDE_DEC <= 2.7 then 'F'
		            when bew.BEW_WAARDE_DEC > 2.7 then 'G'
		            end as label
		FROM    VIEWPOINT_ODS.O_BAE_ELEMENTEN bet
		INNER JOIN
				VIEWPOINT_ODS.O_BAE_PUNT_BEPALINGEN bpb
				ON bet.BPB_ID = bpb.BPB_ID
		INNER JOIN
		        VIEWPOINT_ODS.O_BAE_ELEMENT_WAARDEN bew
				ON  bet.bet_id = bew.BET_ID
	  WHERE bpb.BPB_VAST_OMS =  'energieindex_woningwaardering'
	  AND sysdate between bew.BEW_DATUM_INGANG and bew.BEW_DATUM_TM
  	  AND bew.BEW_WAARDE_DEC IS NOT NULL),
oppervlakte as
		(
		select	bew.bot_id,
				sum(bew.bew_waarde_dec) totaleoppervlakte,
				sum(
				case
				    when bpb.BPB_OMSCHRIJVING = 'Totaal opp. vertrekken'
				    then bew.bew_waarde_dec
				    else 0
				end) woonvertrekken,
				sum(
				case
				    when bpb.BPB_OMSCHRIJVING = 'Totaal oppervlakte  overige ruimten'
				    then bew.bew_waarde_dec
				    else 0
				end) overigeVertrekken
		from	viewpoint_ods.O_BAE_ELEMENTEN bet
		inner join
				viewpoint_ods.O_BAE_ELEMENT_WAARDEN bew
				on	bet.BET_ID = bew.BET_ID
		INNER JOIN 	viewpoint_ods.O_BAE_PUNT_BEPALINGEN bpb
				ON bet.bpb_id = bpb.bpb_id
		where	bpb.BPB_OMSCHRIJVING  IN ('Totaal opp. vertrekken', 'Totaal oppervlakte  overige ruimten')
		  and	sysdate between bew.BEW_DATUM_INGANG and bew.BEW_DATUM_TM
	    group BY bew.bot_id
	    --
	    union all
	    --
	    select  bew.bot_id,
                sum(bew.bew_waarde_dec) as totaleoppervlakte,
                null as woonvertrekken, -- nvt voor kamers
                null as overigevertrekken -- nvt voor kamers
        from    viewpoint_ods.O_BAE_ELEMENT_WAARDEN bew
        inner join
                viewpoint_ods.O_BAE_ELEMENTEN bet
                on  bew.BET_ID = bet.BET_ID
        inner join
                viewpoint_ods.O_BAE_PUNT_BEPALINGEN bpb
                on  bet.BPB_ID = bpb.BPB_ID
        inner join
                viewpoint_ods.O_BAE_OBJECTEN bot
                on  bew.BOT_ID = bot.BOT_ID
        inner join
                viewpoint_ods.O_BAE_OBJECT_SUBTYPEN bop
                on  bot.BOP_ID = bop.BOP_ID
        where   1 = 1
          and   bop.bop_subtype = 'KAMER'
          and   bpb.BPB_CODE = 11 -- Totaal opp. eigen ruimtes
          and	sysdate between bew.BEW_DATUM_INGANG and bew.BEW_DATUM_TM -- alleen op dit moment geldende waarden meenemen
        group by
                bew.bot_id
		),
intramuraal AS
(SELECT bot_id, 'Intramuraal' AS waarde
FROM VIEWPOINT_ODS.O_CON_CONTRACTEN con
WHERE con.CON_STARTDATUM <= trunc(SYSDATE)
AND COALESCE (con.CON_EINDDATUM , trunc(sysdate)) >= trunc(SYSDATE)
AND con.CON_INTRAMURAAL = 'Y'
AND con.CON_ONDERTEKEND = 'Y'
),
epalabel AS (
SELECT bew.bot_id , baw.baw_antwoord AS label
FROM VIEWPOINT_ODS.O_BAE_ELEMENT_WAARDEN bew
INNER JOIN VIEWPOINT_ODS.O_BAE_ELEMENTEN bet
	ON bew.bet_id = bet.bet_id
INNER JOIN VIEWPOINT_ODS.O_BAE_PUNT_BEPALINGEN bpb
	ON bet.BPB_ID = bpb.BPB_ID
INNER JOIN 	VIEWPOINT_ODS.O_BAE_ANTWOORDEN BAW
	ON bew.baw_id = baw.baw_id
WHERE bpb.BPB_VAST_OMS = 'energielabel_woningwaardering'
AND trunc(sysdate) BETWEEN bew.BEW_DATUM_INGANG  AND bew.BEW_DATUM_tm),
doelgroep AS (select rdp.rdp_id , RDP_OMSCHRIJVING,  VREF_CODE as verawaarde
from viewpoint_ods.o_red_doelgroepen rdp
    left join viewpoint_ods.O_T_VERA_REF_DOELGROEP vref
        on rdp.VREF_ID = vref.VREF_ID
where 1=1 ),
soortwoning as
(select baw.baw_antwoord, bew.bot_id
from viewpoint_ods.o_bae_element_waarden bew
inner join
        viewpoint_ods.o_bae_elementen bet
        on bew.bet_id = bet.bet_id
inner join
        viewpoint_ods.o_bae_antwoorden baw
        on bew.baw_id = baw.baw_id
where bet.bet_element = 'Soort woning'
and bew.BEW_DATUM_INGANG <= trunc(sysdate)
and bew.BEW_DATUM_TM  >= trunc(sysdate))

SELECT
        bot.bot_id as identificatie,
        bot.bot_volgnummer as businesskey,
        bot.bot_volgnummer as code,
        bot.bot_omschrijving as omschrijving,
        COALESCE (intramuraal.waarde, bop.bop_omschrijving) as soort,
        bvs.bvs_soort as detailsoort,
        bot.bos_status as status,
        bot.bot_id as adres,
        boi.rayon as rayon,
        boi.bedrijf as eigenaar,
        bcb.bcb_cbs_buurt as corporatieBuurt,
        bwk.bwk_wijk as corporatieWijk,
        -- voor teruggekochte eenheden moet de nieuwe datum exploitatie op basis van de terugkoop worden opgenomen
        CASE WHEN datum_terugkoop.datum_terugkoop IS NOT NULL
             THEN datum_terugkoop.datum_terugkoop
             ELSE bot.bot_datum_ingang
        END as inexploitatiedatum,
        -- Op basis van de inexploitatiedatum en het bouwjaar kan de inexploitatiereden voor een groot deel worden
        -- herleid. Allereerst bepalen of er sprake is van een terugkoop
        CASE WHEN datum_terugkoop.datum_terugkoop IS NOT NULL
             THEN 'Terugkoop'
             WHEN bouwjaar.jaar >= EXTRACT(YEAR from bot.bot_datum_ingang)
             THEN 'Nieuwbouw'
             WHEN bouwjaar.jaar is not null AND ABS(bouwjaar.jaar - EXTRACT(YEAR from bot.bot_datum_ingang)) <= 1
             THEN TO_CHAR(bouwjaar.jaar - EXTRACT(YEAR from bot.bot_datum_ingang))
             WHEN bouwjaar.jaar < EXTRACT(YEAR from bot.bot_datum_ingang)
             THEN 'Aankoop'
             ELSE NULL
        END inexploitatiereden,
        bot.bot_datum_einde as uitexploitatiedatum,
        status_einde.bos_status as uitexploitatiereden,
        bestemming.bbg_intern as bestemming,
        case bop.bop_omschrijving
          when 'Woning' then 1
          when 'Woonwagen' then 0
          when 'Woonboot' then 1
          when 'Kamer' then 0
          else null
        end as zelfstandig,
        vho.bvo_daeb_yn as maatschappelijklabel,
        bot.bot_datum_ingang as inBezitbegindatum,
        bot.bot_datum_einde as inbeziteinddatum,
        CASE WHEN bot.BOT_INGANG_EIGENDOM <= current_date AND (bot.BOT_EINDE_EIGENDOM > current_date OR bot.BOT_EINDE_EIGENDOM is null)
        THEN 'BEH'
        ELSE 'EIG'
        END as bezitsoort,
        bot.bot_datum_ingang as opleverdatum,
        huur.netto as nettohuur,
        huur.bruto as brutohuur,
        case when bop.bop_omschrijving ='Kamer' and (vho.BVO_INDICATIE_KAMERTYPE = 'normale_kamers' or vho.BVO_INDICATIE_KAMERTYPE is null) then null
          else huur.subsidiabel
        end as subsidiabelehuur,
        huur.streefhuur as streefhuur,
        COALESCE (gbo_nen2580.gbo, gbo.gbo) as gebruiksoppervlakte,
        CASE WHEN bot.BOT_INGANG_EIGENDOM <= current_date AND (bot.BOT_EINDE_EIGENDOM > current_date OR bot.BOT_EINDE_EIGENDOM is null)
        THEN 0
        ELSE 100
        END as juridischeigendomspercentage,
        bouwjaar.jaar as bouwjaar,
        rpr.rpr_name as REF_grootboekschema,
        rpr_adm.RPR_CHAR1 REF_administratie,
        CASE WHEN bot.BOT_INGANG_EIGENDOM <= current_date AND (bot.BOT_EINDE_EIGENDOM > current_date OR bot.BOT_EINDE_EIGENDOM is null)
        THEN 'JA'
        ELSE 'NEE'
        END as REF_extern_eigendom,
        status_einde.bos_status as REF_status_einde,
        case when bestemming.BBG_OMSCHRIJVING is not null
            then bestemming.BBG_OMSCHRIJVING
            else bestemming.BBG_INTERN
        end as extraelementen,
        bvs.bvs_soort as publicatielabel,
        COALESCE (epalabel.label, actuele_epa.label)  as energielabel,
		oppervlakte.totaleoppervlakte as totaleoppervlakte,
		bopp.BOPP_VERHUURBAAR_VLOER_OPP as verhuurbaarVloeroppervlakte,
        bot.bot_bag_id as adresseerbaarObjectBasisregistratie,
        vho.bvo_vrije_sector as ref_vrije_sector,
        kamersaantal.aantal as kamersaantal,
        doelgroep.verawaarde as doelgroep,
        doelgroep.RDP_OMSCHRIJVING as ref_doelgroep,
        soortwoning.baw_antwoord as ref_soortwoning,
		bestemming.BBG_OMSCHRIJVING	as ref_bestemming,
		bestemming.VREF_CODE	as ref_bestemming_vera
FROM  viewpoint_ods.o_bae_objecten bot
INNER JOIN
      viewpoint_ods.o_bae_vho vho
      ON  bot.bot_id = vho.bot_id
INNER JOIN
      viewpoint_ods.o_bae_object_subtypen bop
      ON  bot.bop_id = bop.bop_id
INNER JOIN
      VIEWPOINT_ODS.O_RED_PARAMETERS rpr
      ON bot.RPR_ID = rpr.RPR_ID
INNER JOIN
      VIEWPOINT_ODS.O_RED_PARAMETERS rpr_kop
      ON  rpr.rpr_id = rpr_kop.RPR_NUMBER1
      AND rpr_kop.rpr_name = 'Grootboekschema_koppeling'
      AND CURRENT_DATE between rpr_kop.RPR_DATE1 and rpr_kop.RPR_DATE2
INNER JOIN
      VIEWPOINT_ODS.O_RED_PARAMETERS rpr_adm
      ON  rpr_kop.RPR_NUMBER2 = rpr_adm.RPR_ID
LEFT JOIN
      viewpoint_ods.o_bae_object_indeling_info boi
      ON  bot.bot_id = boi.bot_id
LEFT JOIN
      viewpoint_ods.o_bae_vho_typen bvs
      ON  vho.bvs_id = bvs.bvs_id
LEFT  JOIN
      viewpoint_ods.o_bae_cbs_buurten bcb
      ON  vho.bcb_id = bcb.bcb_id
LEFT JOIN
      viewpoint_ods.o_bae_wijken bwk
      ON  vho.bwk_id = bwk.bwk_id
LEFT JOIN
        viewpoint_ods.O_BAE_OPPERVLAKTEN BOPP
        ON VHO.BOT_ID = BOPP.BOT_ID
-- JOIN naar de huur tabel om HUIDIGE huurprijzen per eenheid te koppelen
LEFT JOIN
      huur
      ON  bot.bot_id = huur.bot_id
LEFT JOIN
      status_einde
      ON  bot.bot_id = status_einde.bot_id
LEFT JOIN
      gbo
      ON  bot.bot_id = gbo.bot_id
LEFT JOIN
	gbo_nen2580
      ON  bot.bot_id = gbo_nen2580.bot_id
LEFT JOIN
	  datum_terugkoop
	  ON  bot.BOT_ID = datum_terugkoop.BOT_ID
LEFT JOIN
      bouwjaar
      ON  bot.BOT_ID = bouwjaar.BOT_ID
left join
	  actuele_epa
	  on  bot.BOT_ID = actuele_epa.bot_id
LEFT JOIN
	epalabel
	on  bot.BOT_ID = epalabel.bot_id
left join
	oppervlakte
	 on  bot.BOT_ID = oppervlakte.BOT_ID
LEFT JOIN
	intramuraal
	on  bot.BOT_ID = intramuraal.BOT_ID
LEFT JOIN
	kamersaantal
	on  bot.BOT_ID = kamersaantal.BOT_ID
LEFT JOIN
	doelgroep
	on  bot.rdp_ID = doelgroep.rdp_id
LEFT JOIN
	soortwoning
	on  bot.bot_id = soortwoning.bot_id
LEFT JOIN
	bestemming
	on  bot.bot_id = bestemming.bot_id
WHERE 1=1
