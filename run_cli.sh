echo '{"api_base_url":"http://192.168.0.100/rest/", "app_name":"IMIS Polices Gaya", "app_dir":"IMIS-POL-GAYA", "application_id":"org.openimis.imispolicies.niger", "cli_java_dir": "src/niger/java", "cli_res_dir": "src/niger/res"}' | gh workflow run manual.yml --ref develop --json