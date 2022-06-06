diff --git a/lib/nlattr.c b/lib/nlattr.c
index 86029ad5ead4..f28fb3b9914e 100644
--- a/lib/nlattr.c
+++ b/lib/nlattr.c
@@ -599,6 +599,7 @@ static int __nla_validate_parse(const struct nlattr *head, int len, int maxtype,
 	if (unlikely(rem > 0)) {
 		pr_warn_ratelimited("netlink: %d bytes leftover after parsing attributes in process `%s'.\n",
 				    rem, current->comm);
+		dump_stack();
 		NL_SET_ERR_MSG(extack, "bytes leftover after parsing attributes");
 		if (validate & NL_VALIDATE_TRAILING)
 			return -EINVAL;
