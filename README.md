# Set up Aggregation Service for Aggregatable Reports

This repository contains instructions and scripts to set up and test
the Aggregation Service for [Aggregatable Reports](https://github.com/WICG/conversion-measurement-api/blob/main/AGGREGATION_SERVICE_TEE.md#aggregatable-reports)
locally and on Amazon Web Services [Nitro Enclaves](https://aws.amazon.com/ec2/nitro/nitro-enclaves/).
If you want to learn more about the [Privacy Sandbox](https://privacysandbox.com/)
Aggregation Service for the Attribution Reporting API, aggregatable, and summary
reports click, read the [Aggregation Service proposal](https://github.com/WICG/conversion-measurement-api/blob/main/AGGREGATION_SERVICE_TEE.md#aggregatable-reports).

## Set up local testing

You can process [aggregatable debug reports](https://github.com/WICG/conversion-measurement-api/blob/main/AGGREGATE.md#aggregatable-reports)
locally with the [LocalTestingTool.jar](https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/0.1.2/LocalTestingTool_0.1.2.jar)
into summary reports.
Learn [how to setup debug reports](https://docs.google.com/document/d/1BXchEk-UMgcr2fpjfXrQ3D8VhTR-COGYS1cwK_nyLfg/edit#heading=h.fvp017tkgw79).

*Disclaimer: encrypted reports can **not** be processed with the local testing tool!*

### Using the local testing tool

[Download the local testing tool](https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/0.1.2/LocalTestingTool_0.1.2.jar).
You'll need [Java JRE](https://adoptium.net/) installed to use the tool.

*The `SHA256` of the `LocalTestingTool_{version}.jar` is `4d337c7049de2121df6a856d85981d5f224ff0fd6983975f32f627c2f162b066`
obtained with `openssl sha256 <jar>`.*

Follow the instructions on how to [collect and batch aggregatable reports](#collect-and-batch-aggregatable-reports).
Create an output domain file: `output_domain.avro`. For testing you can use our [sample debug batch](./sampledata/output_debug_reports.avro)
with the corresponding [output domain avro](./sampledata/output_domain.avro).

To aggregate the resulting avro batch `output_debug_reports.avro` file into a summary report
in the same directory where you run the tool, run the following command:

```sh
java -jar LocalTestingTool.jar \
--input_data_avro_file $(pwd)/output_debug_reports.avro \
--domain_avro_file $(pwd)/output_domain.avro \
--output_directory .
```

*Note: The tool expects absolute paths to the input and domain avro
files.*

To see all supported flags for the local testing tool run
`java -jar LocalTestingTool.jar --help`, e.g. you can adjust the noising
epsilon with the `--epsilon` flag or disable noising all together with the
`--no_noising` flag. [See all flags and descriptions](./API.md#local-testing-tool).

## Test on AWS with support for encrypted reports

### Prerequisites

To test the aggregation service with support for encrypted reports, you need the following:

* Have an [AWS account](https://portal.aws.amazon.com/gp/aws/developer/registration/index.html) available to you.
* [Register](https://developer.chrome.com/origintrials/#/view_trial/771241436187197441)
for the Privacy Sandbox Relevance and Measurement origin trial (OT)
* Complete the aggregation service [onboarding form](https://forms.gle/EHoecersGKhpcLPNA)

Once you’ve submitted the onboarding form, we will contact you to verify your information. Then, we’ll send you the remaining instructions and information needed for this setup.</br>
*You won't be able to successfully setup your AWS system without registering for the origin trial and completing the onboarding process!*

To set up aggregation service in AWS you'll use [Terraform](https://www.terraform.io/).

### Clone the repository

Clone the repository into a local folder `<repostory_root>`:

```sh
git clone https://github.com/google/trusted-execution-aggregation-service;
cd trusted-execution-aggregation-service
```

### Set up AWS client

Make sure you [install](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
and [set up](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html)
the latest AWS client.

### Set up Terraform

Change into the `<repository_root>/terraform/aws` folder.

The setup scripts require terraform version `1.0.4`.
You can download Terraform version 1.0.4 from [https://releases.hashicorp.com/terraform/1.0.4/](https://releases.hashicorp.com/terraform/1.0.4/) or
*at your own risk*, you can install and use
[Terraform version manager](https://github.com/tfutils/tfenv) instead.

If you have the Terraform version manager `tfenv` installed, run the following
in your `<repository_root>` to set Terraform to version `1.0.4`.

```sh
tfenv install 1.0.4;
tfenv use 1.0.4
```

We recommend you store the [Terraform state](https://www.terraform.io/language/state)
in a cloud bucket.
Create a S3 bucket via the console/cli, which we'll reference as
`tf_state_bucket_name`.

### Download dependencies

The Terraform scripts depend on 5 packaged jars for Lambda functions deployment.
These jars are hosted on Google Cloud Storage (https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/{version}/{jar_file})
and can be downloaded with the `<repository_root>/terraform/aws/download_dependencies.sh`
script. The downloaded jars will be stored in `<repository_root>/terraform/aws/jars`.
License information of downloaded dependencies can be found in the [DEPENDENCIES.md](./DEPENDENCIES.md)

Run the following script in the `<repository_root>/terraform/aws` folder.

```sh
sh ./download_dependencies.sh
```

For manual download into the `<repository_root>/terraform/aws/jars` folder you
can download them from the links below. The `sha256` was obtained with
`openssl sha256 <jar>`.

| jar download link | sha256 |
| -- | -- |
| [AsgCapacityHandlerLambda_0.1.2.jar](https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/0.1.2/AsgCapacityHandlerLambda_0.1.2.jar) | `40b712ab1c4250b467b4def1781b5240ea18367396bdaa0573e5ac11b058983f` |
| [AwsChangeHandlerLambda_0.1.2.jar](https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/0.1.2/AwsChangeHandlerLambda_0.1.2.jar) | `ebb2525e5dba6031936b87ae3a786726b835636b5296afc9114ce23974761e0c` |
| [AwsFrontendCleanupLambda_0.1.2.jar](https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/0.1.2/AwsFrontendCleanupLambda_0.1.2.jar) | `7a8bb21f18e42327f5e00b4cec44eb80bb4a6ba1fa5bdc760b69b603962576fd` |
| [TerminatedInstanceHandlerLambda_0.1.2.jar](https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/0.1.2/TerminatedInstanceHandlerLambda_0.1.2.jar) | `7543c464be85b29af97967622c929e309697dcd82bf4084a0401c1847908a834` |
| [aws_apigateway_frontend_0.1.2.jar](https://storage.googleapis.com/trusted-execution-aggregation-service-public-artifacts/0.1.2/aws_apigateway_frontend_0.1.2.jar) | `1c1fb14103e2d6c4d44391773e27fed794580e35f0eb4ea42ce4adfc4b49c0af` |

### Set up your deployment environment

We use the following folder structure `<repository_root>/terraform/aws/environments/<environment_name>` to separate
deployment environments.

To set up your first environment (e.g `dev`), copy the `demo` environment. Run
the following commands from the `<repository_root>/terraform/aws/environments`
folder:

```sh
cp -R demo dev
cd dev
```

Make the following adjustments in the `<repository_root>/terraform/aws/environments/dev`
folder:

1. Add the `tf_state_bucket_name` to your `main.tf` by uncommenting and replacing
the values using `<...>`:

    ```sh
    # backend "s3" {
    #   bucket = "<tf_state_bucket_name>"
    #   key    = "<environment_name>.tfstate"
    #   region = "us-east-1"
    # }
    ```

1. Rename `example.auto.tfvars` to `<environment>.auto.tfvars` and
adjust the values with `<...>` using the information you received in the
onboarding email.
Leave all other values as-is for the initial deployment.

    ```sh
    environment = "<environment_name>"
    ...

    assume_role_parameter = "<arn:aws:iam::example:role/example>"

    ...

    alarm_notification_email = "<noreply@example.com>"
    ```

    * environment: name of your environment
    * assume_role_parameter: IAM role given by us in the onboarding email
    * alarm_notification_email: Email to receive alarm notifications. Requires
    confirmation subscription through sign up email sent to this address.

1. Once you’ve adjusted the configuration, run the following in the
`<repository_root>/terraform/aws/environments/dev` folder

    Install all Terraform modules:

    ```sh
    terraform init
    ```

    Get an infrastructure setup plan:

    ```sh
    terraform plan
    ```

    If you see the following output on a fresh project:

    ```terraform
    ...
    Plan: 128 to add, 0 to change, 0 to destroy.
    ```

    you can continue to apply the changes (needs confirmation after the
    planning step)

    ```sh
    terraform apply
    ```

    If your see the following output, your setup was successful:

    ```terraform
    ...
    Apply complete! Resources: 127 added, 0 changed, 0 destroyed.

    Outputs:

    create_job_endpoint = "POST https://xyz.execute-api.us-east-1.amazonaws.com/stage/v1alpha/createJob"
    frontend_api_endpoint = "https://xyz.execute-api.us-east-1.amazonaws.com"
    frontend_api_id = "xyz"
    get_job_endpoint = "GET https://xyz.execute-api.us-east-1.amazonaws.com/stage/v1alpha/getJob"
    ```

    The output has the links to the `createJob` and `getJob` API endpoints.
    These are authenticated endpoints, refer to the
    [Testing the System](#testing-the-system) section to learn how
    to use them.

    *If you run into any issues during deployment of your system, please
    consult the [Troubleshooting](#troubleshooting) and [Support](#support)
    sections.*

### Testing the system

To test the system, you'll need encrypted aggregatable reports in avro batch
format (follow the [collecting and batching instructions](#collect-and-batch-aggregatable-reports))
accessible by the aggregation service.

1. Create an S3 bucket for your input and output data, we will refer to it as
`data_bucket`. This bucket must be created in the same AWS account where
you set up the aggregation service.

1. Copy your reports.avro with batched encrypted aggregatable reports to
`<data_bucket>/input`. To experiment with sample data, you can use our
[sample batch](./sampledata/output_reports.avro)
with the corresponding [output domain avro](./sampledata/output_domain.avro).
Using the sample batch requires overwriting the [privacy budget](https://github.com/WICG/conversion-measurement-api/blob/main/AGGREGATE.md#aggregatable-reports) with
an additional `job_parameters` parameter `"debug_privacy_budget_limit": 100000`.
You can set the value up to a value of 2^31. [Detailed API spec](API.md#createjob-endpoint)
1. Create an aggregation job with the `createJob` API.

    `POST`
    `https://<frontend_api_id>.execute-api.us-east-1.amazonaws.com/stage/v1alpha/createJob`

    ```json
    {
        "input_data_blob_prefix": "input/reports.avro",
        "input_data_bucket_name": "<data_bucket>",
        "output_data_blob_prefix": "output/summary_report.avro",
        "output_data_bucket_name": "<data_bucket>",
        "job_parameters": {
            "attribution_report_to": "<your_attribution_domain>",
            "output_domain_blob_prefix": "domain/domain.avro",
            "output_domain_bucket_name": "<data_bucket>"
        },
        "job_request_id": "test01"
    }
    ```

    Note: This API requires authentication. Follow the [AWS instructions](https://aws.amazon.com/premiumsupport/knowledge-center/iam-authentication-api-gateway/)
    for sending an authenticated request.

1. Check the status of your job with the `getJob` API, replace values in `<...>`

    `GET` `https://<frontend_api_id>.execute-api.us-east-1.amazonaws.com/stage/v1alpha/getJob?job_request_id=test01`
    Note: This API requires authentication. Follow the [AWS instructions](https://aws.amazon.com/premiumsupport/knowledge-center/iam-authentication-api-gateway/)
    for sending an authenticated request. [Detailed API spec](API.md#getjob-endpoint)

## Collect and batch aggregatable reports

Both the local testing tool and the aggregation service running on AWS Nitro
Enclave expect aggregatable reports batched in the following
[Avro](https://avro.apache.org/) format.

```avro
{
  "type": "record",
  "name": "AggregatableReport",
  "fields": [
    {
      "name": "payload",
      "type": "bytes"
    },
    {
      "name": "key_id",
      "type": "string"
    },
    {
      "name": "shared_info",
      "type": "string"
    }
  ]
}
```

Additionally an output domain file is needed to declare all expected aggregation
keys for aggregating the aggregatable reports (keys not listed in the domain
file won't be aggregated)

```avro
{
 "type": "record",
 "name": "AggregationBucket",
 "fields": [
   {
     "name": "bucket",
     "type": "bytes"
     /* A single bucket that appears in
     the aggregation service output.
     128-bit integer encoded as a
     16-byte big-endian byte string. */
   }
 ]
}
```

[Review code snippets](./COLLECTING.md) which demonstrate how to collect and
batch aggregatable reports.

## Troubleshooting

The following error message points to a potential lack of instance availability.
If you encounter this situation, run `terraform destroy` to remove your
deployment and run `terraform apply` again.

```txt
Error: Error creating Auto Scaling Group: ValidationError: You must use a valid 
fully-formed launch template. Your requested instance type (m5.2xlarge) is not
supported in your requested Availability Zone (us-east-1e).
Please retry your request by not specifying an Availability Zone or choosing
us-east-1a, us-east-1b, us-east-1c, us-east-1d, us-east-1f.
```

## Support

You can reach out to us for support through creating issues on this repository
or sending us an email at aggregation-service-support\<at>google.com.
This address is monitored and only visible to selected support staff.

## General security notes

- The [VPC subnet property](./terraform/aws/services/worker/network.tf#L51)
`map_public_ip_on_launch` is currently set to `true` which assigns a public
IP address to all instances in the subnet. This allows for easier console
access, yet is considered a risk and will be addressed in a future release.
- The worker [VPC security group](./terraform/aws/services/worker/network.tf#L99)
currently allows for inbound connections on port 22 from any source IP.
This is considered a risk and will be addressed in a future release.

## License

Apache 2.0 - See [LICENSE](LICENSE) for more information.
