CREATE SEQUENCE lims_manifest_id_seq;
CREATE TABLE public.lims_manifest
(
    id bigint NOT NULL DEFAULT nextval('lims_manifest_id_seq'),
    uuid character varying(100),
    manifest_id character varying(100),
    sending_facility_id character varying(100),
    sending_facility_name character varying(100),
    receiving_lab_id character varying(100),
    receiving_lab_name character varying(100),
    date_scheduled_for_pickup character varying(100),
    temperature_at_pickup character varying(100),
    sample_packaged_by character varying(100),
    courier_rider_name character varying(100),
    courier_contact character varying(100),
    manifest_status character varying(100),
	create_date timestamp,
	current_status character varying(100),
    PRIMARY KEY (id)
);
ALTER SEQUENCE lims_manifest_id_seq OWNED BY lims_manifest.id;


CREATE SEQUENCE lims_sample_id_seq;
CREATE TABLE public.lims_sample
(
    id bigint NOT NULL DEFAULT nextval('lims_sample_id_seq'),
    uuid character varying(100),
    sample_id character varying(100),
    pid character varying(100),
    patient_id JSONB,
    sample_type character varying(100),
    sample_ordered_by character varying(100),
    sample_order_date character varying(100),
    sample_collected_by character varying(100),
    sample_collection_date character varying(100),
    sample_Collection_time character varying(100),
    date_sample_sent character varying(100),
    indication_vl_test character varying(100),
    first_name character varying(100),
    surname character varying(100),
    Sex character varying(100),
    Age character varying(100),
    date_of_birth character varying(100),
    pregnant_breast_feeding_status character varying(100),
    art_commencement_date character varying(100),
    drug_regimen character varying(100),
    sending_facility_id character varying(100),
    sending_facility_name character varying(100),
	priority character varying(100),
	priority_reason character varying(100),
    manifest_record_id bigint,
    PRIMARY KEY (id)
);
ALTER SEQUENCE lims_sample_id_seq OWNED BY lims_sample.id;


CREATE SEQUENCE lims_config_id_seq;
CREATE TABLE public.lims_config
(
    id bigint NOT NULL DEFAULT nextval('lims_config_id_seq'),
    uuid character varying(100),
    config_name character varying(100),
    config_email character varying(100),
    config_password character varying(100),
    server_url character varying(100),
    create_date character varying(100),
    test_facility_name character varying(100),
    test_facility_datim_code character varying(100),
    PRIMARY KEY (id)
);
ALTER SEQUENCE lims_config_id_seq OWNED BY lims_config.id;


CREATE SEQUENCE lims_result_id_seq;
CREATE TABLE public.lims_result
(
    id bigint NOT NULL DEFAULT nextval('lims_result_id_seq'),
    uuid character varying(100),
    sample_id character varying(100),
    pcr_lab_sample_number character varying(100),
    visit_date character varying(100),
    date_sample_received_at_pcr_lab character varying(100),
    result_date character varying(100),
	test_result character varying(100),
    assay_date character varying(100),
	approval_date character varying(100),
    date_result_dispatched character varying(100),
    sample_status character varying(100),
    sample_testable character varying(100),
    manifest_record_id bigint,
    transfer_status character varying(100),
    tested_by character varying(100),
    approved_by character varying(100),
    date_transferred_out character varying(100),
    reason_not_tested character varying(100),
    other_rejection_reason character varying(100),
    sending_pcr_lab_id character varying(100),
    sending_pcr_lab_name character varying(100),
    PRIMARY KEY (id)
);
ALTER SEQUENCE lims_result_id_seq OWNED BY lims_result.id;

