select
    coalesce(adres_pe.id_s, -1) as adres_id
,   coalesce(tijd.tijdkey, 'Unknown') as calender_id
,   coalesce(di01905cluster_pe.id_s, -1) as di01905cluster_id
,   coalesce(eenheid_pe.id_s, -1) as eenheid_id
,   coalesce(eenheidtoestand_pe.id_s, -1) as eenheidtoestand_id
,   cast(sum(case
        when eenheidtoestand_pe.status = 'LEE'
        and eenheidtoestand_pe.begindatum <= tijd.einddatum
        and (eenheidtoestand_pe.einddatum >= tijd.begindatum or eenheidtoestand_pe.einddatum is null)
        then case
            when eenheidtoestand_pe.einddatum > tijd.einddatum or eenheidtoestand_pe.einddatum is null
            then tijd.einddatum
            else eenheidtoestand_pe.einddatum
        end - case
            when eenheidtoestand_pe.begindatum < tijd.begindatum
            then tijd.begindatum
            else eenheidtoestand_pe.begindatum
        end
    end + 1) as bigint) as kg00168
from dv_9990.tijd tijd
cross join lateral (select * from dv_9990.s_h_eenheidtoestand_ssm where dv_start_dts <= tijd.einddatum and dv_end_dts > tijd.einddatum) eenheidtoestand_pe
inner join dv_9990.l_eenheid_eenheidtoestand_ssm l_eenheid_eenheidtoestand_ssm_pe
    on eenheidtoestand_pe.id_h_eenheidtoestand = l_eenheid_eenheidtoestand_ssm_pe.id_h_eenheidtoestand
    and l_eenheid_eenheidtoestand_ssm_pe.dv_start_dts <= tijd.einddatum
    and l_eenheid_eenheidtoestand_ssm_pe.dv_end_dts > tijd.einddatum
inner join dv_9990.s_h_eenheid_ssm eenheid_pe
    on l_eenheid_eenheidtoestand_ssm_pe.id_h_eenheid = eenheid_pe.id_h_eenheid
    and eenheid_pe.dv_start_dts <= tijd.einddatum
    and eenheid_pe.dv_end_dts > tijd.einddatum
left join dv_9990.l_adres_eenheid_ssm l_adres_eenheid_ssm_pe
    on eenheid_pe.id_h_eenheid = l_adres_eenheid_ssm_pe.id_h_eenheid
    and l_adres_eenheid_ssm_pe.dv_start_dts <= tijd.einddatum
    and l_adres_eenheid_ssm_pe.dv_end_dts > tijd.einddatum
left join dv_9990.s_h_adres_ssm adres_pe
    on l_adres_eenheid_ssm_pe.id_h_adres = adres_pe.id_h_adres
    and adres_pe.dv_start_dts <= tijd.einddatum
    and adres_pe.dv_end_dts > tijd.einddatum
left join lateral (select
        l_cluster_eenheid_ssm.id_h_eenheid
    ,   di01905cluster.id_s
    from dv_9990.l_cluster_eenheid_ssm
    inner join dv_9990.s_h_cluster_ssm di01905cluster
        on l_cluster_eenheid_ssm.id_h_cluster = di01905cluster.id_h_cluster
        and di01905cluster.dv_start_dts <= tijd.einddatum
        and di01905cluster.dv_end_dts > tijd.einddatum
    where di01905cluster.soort = 'FIN'
) di01905cluster_pe
    on eenheid_pe.id_h_eenheid = di01905cluster_pe.id_h_eenheid
where eenheid_pe.inExploitatiedatum <= tijd.einddatum
and (eenheid_pe.uitExploitatiedatum is null or eenheid_pe.uitExploitatiedatum >= tijd.begindatum)
and eenheid_pe.inbezitbegindatum <= tijd.einddatum
and (eenheid_pe.inbeziteinddatum is null or eenheid_pe.inbeziteinddatum > tijd.begindatum)
and (eenheid_pe.bezitsoort = 'EIG' or eenheid_pe.bezitsoort = 'BEH'
    and eenheid_pe.bezitdetailsoort = 'BEC')
and eenheidtoestand_pe.begindatum <= tijd.einddatum
and (eenheidtoestand_pe.einddatum >= tijd.begindatum or eenheidtoestand_pe.einddatum is null)
and eenheidtoestand_pe.status = 'LEE'
group by coalesce(adres_pe.id_s, -1)
  , coalesce(tijd.tijdkey, 'Unknown')
  , coalesce(di01905cluster_pe.id_s, -1)
  , coalesce(eenheid_pe.id_s, -1)
  , coalesce(eenheidtoestand_pe.id_s, -1)
